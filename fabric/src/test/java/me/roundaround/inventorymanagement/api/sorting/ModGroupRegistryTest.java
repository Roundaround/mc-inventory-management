package me.roundaround.inventorymanagement.api.sorting;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.config.SortMode;
import me.roundaround.inventorymanagement.inventory.sorting.SortContext;
import me.roundaround.inventorymanagement.inventory.sorting.itemstack.ItemNameComparator;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import me.roundaround.trove.env.Env;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the mod-registry consult path: a group registered via
 * {@link ItemVariantRegistry#registerModGroup} is actually consulted by {@link ItemNameComparator},
 * is consulted strictly after the built-in {@code COLOR} families (so built-ins win overlaps), and
 * honors its own enabled supplier. Uses {@link ItemVariantRegistry#clearModGroupsForTest()} for
 * isolation since the mod-group list is a process-global static.
 *
 * <p>Located in the {@code api.sorting} package so it can reach the package-private test helper.
 */
public class ModGroupRegistryTest extends BaseMinecraftTest {
  private static final UUID PLAYER = UUID.randomUUID();

  @BeforeAll
  static void bootstrapEnv() {
    Env.bootstrap(Env.CLIENT);
  }

  @BeforeEach
  @AfterEach
  void isolate() {
    ItemVariantRegistry.clearModGroupsForTest();
  }

  private static SortContext grouping() {
    return new SortContext(PLAYER, SortMode.ALPHABETICAL, false, true);
  }

  /** A predicate that never matches any real item, for ordering-only assertions. */
  private static Predicate<ItemStack> never() {
    return (stack) -> false;
  }

  // A "gems" family no built-in matches in the empty-tag harness, so this class stays robust to
  // whatever groups other test classes append to the global, append-only COLOR registry.
  private static final Predicate<ItemStack> IS_GEM =
      (stack) -> stack.is(Items.DIAMOND) || stack.is(Items.EMERALD);

  private static VariantGroup gemGroup(boolean enabled) {
    // Anchor the family at the diamond slot; both gems sort-key to [diamond, ownDescId].
    return VariantGroup.byPredicate(IS_GEM, Items.DIAMOND.getDescriptionId(), () -> enabled);
  }

  private static List<ItemStack> sortedGemSample() {
    // EGG's description id (item.minecraft.egg) sorts strictly between diamond and emerald, so when
    // the gems are NOT grouped it splits them; when grouped, the two gems cluster at the diamond
    // anchor and EGG no longer separates them.
    ArrayList<ItemStack> stacks = Lists.newArrayList(
        new ItemStack(Items.EMERALD),
        new ItemStack(Items.DIAMOND),
        new ItemStack(Items.EGG)
    );
    Collections.shuffle(stacks);
    stacks.sort(new ItemNameComparator(grouping()));
    return stacks;
  }

  private static boolean gemsContiguous(List<ItemStack> sorted) {
    int first = -1;
    int last = -1;
    int count = 0;
    for (int i = 0; i < sorted.size(); i++) {
      if (IS_GEM.test(sorted.get(i))) {
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
  void modGroupIsConsultedAndClusters() {
    ItemVariantRegistry.registerModGroup(gemGroup(true));

    assertTrue(gemsContiguous(sortedGemSample()),
        "A registered mod group should be consulted and cluster its members");
  }

  @Test
  void disabledModGroupIsSkipped() {
    ItemVariantRegistry.registerModGroup(gemGroup(false));

    assertFalse(gemsContiguous(sortedGemSample()),
        "A mod group with enabled=()->false must be skipped (gems fall back to alphabetical)");
  }

  @Test
  void builtinsAreConsultedBeforeModGroups() {
    // The "built-in wins an overlap" guarantee reduces to "every COLOR built-in is consulted before
    // any mod group" under first-match-wins. Proven structurally without polluting the global,
    // append-only COLOR registry (which has no remove and whose tag-based built-ins never match in
    // the empty-tag test harness anyway).
    VariantGroup mod = VariantGroup.byPredicate(never(), "anchor.mod", () -> true);
    ItemVariantRegistry.registerModGroup(mod);

    List<VariantGroup> effective = ItemVariantRegistry.effectiveGroups();
    int lastColorIndex = ItemVariantRegistry.COLOR.list().isEmpty()
        ? -1
        : effective.indexOf(ItemVariantRegistry.COLOR.list().get(ItemVariantRegistry.COLOR.list().size() - 1));
    int modIndex = effective.indexOf(mod);

    assertTrue(modIndex > lastColorIndex,
        "Mod groups must be consulted strictly after all COLOR built-ins (built-ins win overlaps)");
  }

  @Test
  void modRegistrationOrderIsTheTiebreak() {
    // Two mod groups matching the SAME (uniquely-prefixed, no-built-in-overlap) predicate. Under
    // first-match-wins, the FIRST registrant is the one the comparator reaches first among the mod
    // groups, so it wins the overlap. Use a predicate no COLOR built-in matches to stay robust to
    // whatever spawn-egg/other groups other test classes may have appended to the global COLOR.
    Predicate<ItemStack> diamonds = (stack) -> stack.is(Items.DIAMOND);
    VariantGroup first = VariantGroup.byPredicate(diamonds, "anchor.first", () -> true);
    VariantGroup second = VariantGroup.byPredicate(diamonds, "anchor.second", () -> true);
    ItemVariantRegistry.registerModGroup(first);
    ItemVariantRegistry.registerModGroup(second);

    ItemStack diamond = new ItemStack(Items.DIAMOND);
    VariantGroup firstMatch = ItemVariantRegistry.effectiveGroups().stream()
        .filter((g) -> g.predicate().test(diamond))
        .findFirst()
        .orElseThrow();
    assertEquals("anchor.first", firstMatch.groupProducer().apply(grouping(), diamond).get(0),
        "First-registered mod group wins an overlap");
  }

  @Test
  void effectiveGroupsOrderIsColorThenModsThenDynamic() {
    int colorCount = ItemVariantRegistry.COLOR.list().size();
    int dynamicCount = ItemVariantRegistry.DYNAMIC.list().size();

    VariantGroup modA = VariantGroup.byPredicate(
        never(), "anchor.a", () -> true);
    VariantGroup modB = VariantGroup.byPredicate(
        never(), "anchor.b", () -> true);
    ItemVariantRegistry.registerModGroup(modA);
    ItemVariantRegistry.registerModGroup(modB);

    List<VariantGroup> effective = ItemVariantRegistry.effectiveGroups();
    assertEquals(colorCount + 2 + dynamicCount, effective.size(),
        "effectiveGroups = COLOR ++ modGroups ++ DYNAMIC");

    // Mod groups sit immediately after the COLOR built-ins, in registration order.
    assertEquals(modA, effective.get(colorCount), "First mod group follows the last built-in");
    assertEquals(modB, effective.get(colorCount + 1), "Mod groups preserve registration order");
  }
}
