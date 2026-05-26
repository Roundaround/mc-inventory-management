package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.api.sorting.GroupDefs;
import me.roundaround.inventorymanagement.api.sorting.ItemVariantRegistry;
import me.roundaround.inventorymanagement.api.sorting.VariantGroup;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.SortMode;
import me.roundaround.inventorymanagement.inventory.sorting.SortContext;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import me.roundaround.trove.env.Env;
import me.roundaround.trove.util.PathAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the item-grouping sort path.
 *
 * <p>Harness constraint: {@code BaseMinecraftTest} installs an empty-tag registry lookup, so
 * {@code stack.is(tag)} is FALSE for every tag. Tag-based families (wool, planks, ...) cannot be
 * exercised through the comparator here; only PREDICATE families actually match. Clustering and
 * toggle behavior are proven with a predicate family; the tag-based wiring is validated
 * structurally via {@link GroupDefs#ALL} and the config map.
 *
 * <p>{@link ItemVariantRegistry#COLOR} is process-global and append-only, so to stay isolated this
 * class registers exactly ONE controllable spawn-egg group (guarded by a mutable flag) and reuses
 * it across the clustering/toggle assertions instead of registering a group per test.
 */
public class ItemGroupingTest extends BaseMinecraftTest {
  private static final UUID PLAYER = UUID.randomUUID();

  /** Mutable gate backing the single spawn-egg group registered onto COLOR for this class. */
  private static final boolean[] SPAWN_EGGS_ENABLED = { true };

  @BeforeAll
  static void setUpGrouping() {
    Env.bootstrap(Env.CLIENT);
    bootstrapPathAccessor();

    Predicate<ItemStack> isSpawnEgg =
        (stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_spawn_egg");
    VariantGroup spawnEggs = VariantGroup.byPredicate(
        isSpawnEgg, Items.WHITE_DYE.getDescriptionId(), () -> SPAWN_EGGS_ENABLED[0]);
    ItemVariantRegistry.COLOR.register(spawnEggs);
  }

  private static SortContext grouping() {
    return new SortContext(PLAYER, SortMode.ALPHABETICAL, false, true);
  }

  private static List<ItemStack> sortedSample() {
    // Spawn eggs interleaved with non-eggs whose REGISTRY-ID PATHS fall between the egg paths
    // (allay_*, creeper_*, zombie_*): bamboo (b) between allay/creeper, diamond (d) between
    // creeper/zombie. Sorting in this harness compares raw description ids (item.minecraft.<path>).
    ArrayList<ItemStack> stacks = Lists.newArrayList(
        new ItemStack(Items.ZOMBIE_SPAWN_EGG),
        new ItemStack(Items.ALLAY_SPAWN_EGG),
        new ItemStack(Items.CREEPER_SPAWN_EGG),
        new ItemStack(Items.BAMBOO),
        new ItemStack(Items.DIAMOND)
    );
    Collections.shuffle(stacks);
    stacks.sort(new ItemNameComparator(grouping()));
    return stacks;
  }

  private static boolean eggsContiguous(List<ItemStack> sorted) {
    int first = -1;
    int last = -1;
    int count = 0;
    for (int i = 0; i < sorted.size(); i++) {
      if (BuiltInRegistries.ITEM.getKey(sorted.get(i).getItem()).getPath().endsWith("_spawn_egg")) {
        if (first < 0) {
          first = i;
        }
        last = i;
        count++;
      }
    }
    return first >= 0 && (last - first + 1) == count;
  }

  @Test
  void predicateFamilyClustersWhenEnabled() {
    SPAWN_EGGS_ENABLED[0] = true;
    assertTrue(eggsContiguous(sortedSample()),
        "Spawn eggs should be contiguous when grouping is enabled: " + describe(sortedSample()));
  }

  @Test
  void toggleGatesGroupingAtRuntime() {
    SPAWN_EGGS_ENABLED[0] = true;
    assertTrue(eggsContiguous(sortedSample()), "Enabled: spawn eggs should cluster.");

    // Flip the live gate; the comparator reads it at map time, so a fresh sort reflects it with no
    // re-registration. Disabled => eggs fall back to plain alphabetical and the interlopers split
    // them apart.
    SPAWN_EGGS_ENABLED[0] = false;
    try {
      List<ItemStack> off = sortedSample();
      assertFalse(eggsContiguous(off),
          "Disabled: spawn eggs should no longer cluster: " + describe(off));
    } finally {
      SPAWN_EGGS_ENABLED[0] = true;
    }
  }

  @Test
  void firstMatchWinsOrderingInvariants() {
    List<String> ids = GroupDefs.ALL.stream().map(GroupDefs.GroupDef::id).toList();
    assertTrue(ids.indexOf("glazed_terracotta") < ids.indexOf("terracotta"),
        "glazed_terracotta must precede terracotta");
    assertTrue(ids.indexOf("stained_glass_pane") < ids.indexOf("stained_glass"),
        "stained_glass_pane must precede stained_glass");
    assertTrue(ids.indexOf("concrete_powder") < ids.indexOf("concrete"),
        "concrete_powder must precede concrete");
  }

  @Test
  void configRegistersOneTogglePerGroup() {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    config.init();

    assertEquals(GroupDefs.ALL.size(), config.groupToggles.size(),
        "One toggle per GroupDefs.ALL entry");
    for (GroupDefs.GroupDef def : GroupDefs.ALL) {
      assertTrue(config.groupToggles.containsKey(def.id()), "Missing toggle for " + def.id());
      assertTrue(config.groupToggles.get(def.id()).getDefaultValue(),
          "Toggle for " + def.id() + " should default true");
      assertEquals("grouping", config.groupToggles.get(def.id()).getPath().getGroup(),
          "Toggle for " + def.id() + " should live under the grouping section");
    }

    // Live read: groupEnabled reflects the current committed value.
    assertTrue(config.groupEnabled("wool").getAsBoolean(), "wool enabled by default after init");
    config.groupToggles.get("wool").setValue(false);
    config.groupToggles.get("wool").commit();
    assertFalse(config.groupEnabled("wool").getAsBoolean(), "groupEnabled should read live value");
    config.groupToggles.get("wool").setValue(true);
    config.groupToggles.get("wool").commit();

    // Unknown id is false-safe.
    assertFalse(config.groupEnabled("does_not_exist").getAsBoolean(), "Unknown id should be false");
  }

  /**
   * Bootstrap a temp-dir PathAccessor so the file-backed config store can init in a unit test (the
   * loader normally does this).
   */
  private static void bootstrapPathAccessor() {
    final Path tmp;
    try {
      tmp = Files.createTempDirectory("im-grouping-test");
      tmp.toFile().deleteOnExit();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    PathAccessor.bootstrap(new PathAccessor() {
      @Override
      public Path getGameDir() {
        return tmp;
      }

      @Override
      public Path getConfigDir() {
        return tmp;
      }

      @Override
      public boolean isWorldDirAccessible() {
        return false;
      }

      @Override
      @Nullable
      public Path getWorldDir() {
        return null;
      }
    });
  }

  private static String describe(List<ItemStack> stacks) {
    return stacks.stream()
        .map((s) -> BuiltInRegistries.ITEM.getKey(s.getItem()).getPath())
        .toList()
        .toString();
  }
}
