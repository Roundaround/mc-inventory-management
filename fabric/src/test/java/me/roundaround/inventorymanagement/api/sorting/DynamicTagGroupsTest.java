package me.roundaround.inventorymanagement.api.sorting;

import com.mojang.serialization.Lifecycle;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import me.roundaround.trove.env.Env;
import me.roundaround.trove.util.PathAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the pure discovery/parse half of the datapack tag-driven grouping path
 * ({@link DynamicTagGroups#discover}) over a simulated item-tag set, plus the
 * {@link InventoryManagementConfig#groupEnabledDynamic} reconciliation.
 *
 * <p>The live loader reload WIRING (whether the Fabric/NeoForge/Forge tag-sync events actually fire
 * and invoke {@code rebuild}) is NOT unit-testable here — {@code BaseMinecraftTest} never fires a
 * loader tag-sync event — and is called out for manual in-game verification.
 */
public class DynamicTagGroupsTest extends BaseMinecraftTest {
  @BeforeAll
  static void setUp() {
    Env.bootstrap(Env.CLIENT);
    bootstrapPathAccessor();
  }

  /** A provider whose ITEM registry lookup advertises exactly the given tag keys via listTags(). */
  private static HolderLookup.Provider providerWithItemTags(List<TagKey<Item>> tags) {
    return new HolderLookup.Provider() {
      @Override
      @NotNull
      public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return Stream.of(Registries.ITEM);
      }

      @SuppressWarnings("unchecked")
      @Override
      @NotNull
      public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(
          @NotNull ResourceKey<? extends Registry<? extends T>> registryKey) {
        if (!Registries.ITEM.equals(registryKey)) {
          return Optional.empty();
        }
        return Optional.of((HolderLookup.RegistryLookup<T>) itemLookupWithTags(tags));
      }
    };
  }

  private static HolderLookup.RegistryLookup<Item> itemLookupWithTags(List<TagKey<Item>> tags) {
    Registry<Item> delegate = BuiltInRegistries.ITEM;
    return new HolderLookup.RegistryLookup<>() {
      @Override
      @NotNull
      public ResourceKey<? extends Registry<? extends Item>> key() {
        return Registries.ITEM;
      }

      @Override
      @NotNull
      public Lifecycle registryLifecycle() {
        return Lifecycle.stable();
      }

      @Override
      @NotNull
      public Optional<Holder.Reference<Item>> get(@NotNull ResourceKey<Item> key) {
        return delegate.get(key);
      }

      @Override
      @NotNull
      public Stream<Holder.Reference<Item>> listElements() {
        return delegate.listElements();
      }

      @Override
      @NotNull
      public Optional<HolderSet.Named<Item>> get(@NotNull TagKey<Item> tagKey) {
        return Optional.of(HolderSet.emptyNamed(this, tagKey));
      }

      @Override
      @NotNull
      public Stream<HolderSet.Named<Item>> listTags() {
        // Membership is irrelevant to discovery (which only reads key()); emptyNamed suffices.
        return tags.stream().map((tag) -> HolderSet.emptyNamed(this, tag));
      }
    };
  }

  private static TagKey<Item> itemTag(String namespace, String path) {
    return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
  }

  @Test
  void onlyGroupingPrefixedTagsBecomeGroups() {
    HolderLookup.Provider provider = providerWithItemTags(List.of(
        itemTag("mymod", "grouping/gems"),
        itemTag("mymod", "ores"),
        itemTag("minecraft", "planks"),
        itemTag("other", "grouping/spices")
    ));

    List<VariantGroup> groups = DynamicTagGroups.discover(provider);
    assertEquals(2, groups.size(), "Only grouping/-prefixed tags should produce groups");
  }

  @Test
  void groupsAreSortedByFullTagId() {
    // Intentionally supply out-of-order so the sort is observable.
    HolderLookup.Provider provider = providerWithItemTags(List.of(
        itemTag("zmod", "grouping/zeta"),
        itemTag("amod", "grouping/alpha"),
        itemTag("mmod", "grouping/middle")
    ));

    List<VariantGroup> groups = DynamicTagGroups.discover(provider);
    // The anchor (first sort-key element) is the tag language key; verify ascending tag-id order via
    // the corresponding language keys.
    List<String> anchors = groups.stream().map(DynamicTagGroupsTest::anchorOf).toList();
    assertEquals(List.of(
        itemTag("amod", "grouping/alpha").location().toLanguageKey(),
        itemTag("mmod", "grouping/middle").location().toLanguageKey(),
        itemTag("zmod", "grouping/zeta").location().toLanguageKey()
    ), anchors, "Dynamic groups must be ordered by full tag id");
  }

  @Test
  void anchorIsTheTagLanguageKey() {
    TagKey<Item> tag = itemTag("mymod", "grouping/gems");
    HolderLookup.Provider provider = providerWithItemTags(List.of(tag));

    List<VariantGroup> groups = DynamicTagGroups.discover(provider);
    assertEquals(1, groups.size());
    assertEquals(tag.location().toLanguageKey(), anchorOf(groups.get(0)),
        "Anchor should be the tag's language key (e.g. tag.item.mymod.grouping.gems)");
  }

  @Test
  void emptyTagSetYieldsNoGroups() {
    HolderLookup.Provider provider = providerWithItemTags(List.of());
    assertTrue(DynamicTagGroups.discover(provider).isEmpty(),
        "No grouping/ tags should yield an empty dynamic set");
  }

  @Test
  void dynamicEnablementReconciles() {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    config.init();

    String id = "mymod:grouping/gems";

    // Master on, not individually disabled => enabled.
    config.dynamicGroupsEnabled.setValue(true);
    config.dynamicGroupsEnabled.commit();
    config.disabledDynamicGroups.setValue(List.of());
    config.disabledDynamicGroups.commit();
    assertTrue(config.groupEnabledDynamic(id).getAsBoolean(), "Enabled when master on and not disabled");

    // Master off => everything disabled.
    config.dynamicGroupsEnabled.setValue(false);
    config.dynamicGroupsEnabled.commit();
    assertFalse(config.groupEnabledDynamic(id).getAsBoolean(), "Master off disables all dynamic groups");

    // Master on but this id individually disabled => only that id disabled.
    config.dynamicGroupsEnabled.setValue(true);
    config.dynamicGroupsEnabled.commit();
    config.disabledDynamicGroups.setValue(List.of(id));
    config.disabledDynamicGroups.commit();
    assertFalse(config.groupEnabledDynamic(id).getAsBoolean(), "Individually disabled id is skipped");
    assertTrue(config.groupEnabledDynamic("other:grouping/x").getAsBoolean(),
        "A different id remains enabled");

    // Reset for any later tests in the class.
    config.disabledDynamicGroups.setValue(List.of());
    config.disabledDynamicGroups.commit();
  }

  private static String anchorOf(VariantGroup group) {
    // groupProducer's first element is the cluster anchor; stack arg is unused by the name-key
    // producer, so an empty stack is fine.
    return group.groupProducer().apply(null, net.minecraft.world.item.ItemStack.EMPTY).get(0);
  }

  private static void bootstrapPathAccessor() {
    final Path tmp;
    try {
      tmp = Files.createTempDirectory("im-dynamic-test");
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
}
