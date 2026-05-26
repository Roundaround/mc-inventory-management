package me.roundaround.inventorymanagement.api.sorting;

import me.roundaround.inventorymanagement.inventory.sorting.itemstack.ItemNameComparator;
import me.roundaround.inventorymanagement.inventory.sorting.itemstack.RegistryBackedComparator;
import me.roundaround.inventorymanagement.config.SortMode;
import me.roundaround.inventorymanagement.inventory.sorting.SortContext;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import me.roundaround.trove.env.Env;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the public comparator tie-break registry. Verifies that registered contributions order
 * otherwise-tying stacks, never override the primary alphabetical/metadata order, behave as a no-op
 * when the registry is empty, leave items unrecognized by a {@code registerKey} predicate untouched,
 * and honor the priority + registration-order tie-break contract.
 *
 * <p>Located in the {@code api.sorting} package so it can reach the package-private
 * {@link SortComparatorRegistry#clearForTest()} helper, mirroring {@link ModGroupRegistryTest}.
 */
public class SortComparatorRegistryTest extends BaseMinecraftTest {
  @BeforeAll
  static void bootstrapEnv() {
    Env.bootstrap(Env.CLIENT);
  }

  @BeforeEach
  @AfterEach
  void isolate() {
    SortComparatorRegistry.clearForTest();
  }

  private static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath("imtest", path);
  }

  private static ItemStack diamond(int count) {
    return new ItemStack(Items.DIAMOND, count);
  }

  // (a) A key contribution orders stacks the rest of the chain treats as tied.
  @Test
  void ordersOtherwiseTyingItems() {
    ItemStack few = diamond(1);
    ItemStack many = diamond(8);

    // Before any registration the slot is a no-op for the tied pair.
    assertEquals(0, RegistryBackedComparator.getInstance().compare(few, many),
        "Empty registry should not order two diamond stacks");

    // Order by descending count: the fuller stack should sort earlier.
    SortComparatorRegistry.registerKey(
        id("by_count"), 100, (stack) -> stack.is(Items.DIAMOND), (stack) -> -stack.getCount());

    assertTrue(RegistryBackedComparator.getInstance().compare(many, few) < 0,
        "Fuller stack should sort before emptier stack");
    assertTrue(RegistryBackedComparator.getInstance().compare(few, many) > 0,
        "Comparison should be antisymmetric");
  }

  // (b) The tie-break slot cannot override the primary alphabetical order of distinct-name items.
  @Test
  void doesNotChangePrimaryOrderOfNonTyingItems() {
    SortContext context = new SortContext(PLAYER_UUID, SortMode.ALPHABETICAL, false, false);
    ItemNameComparator name = new ItemNameComparator(context);

    ItemStack apple = new ItemStack(Items.APPLE);
    ItemStack diamond = diamond(1);

    int baseline = name.compare(apple, diamond);
    assertNotEquals(0, baseline, "APPLE and DIAMOND should be distinguished by name");

    // Register a contribution that would order them OPPOSITELY in the tie-break slot.
    SortComparatorRegistry.register(id("force_reverse"), 1, Comparator.comparingInt((stack) ->
        stack.is(Items.APPLE) ? 1 : 0));

    int withSlot = RegistryBackedComparator.getInstance().compare(apple, diamond);
    assertTrue(withSlot != 0, "The contribution itself does fire when consulted directly");

    // Full chain: name decides first, so the registry slot never gets consulted for this pair.
    me.roundaround.inventorymanagement.inventory.sorting.itemstack.ItemStackComparator chain =
        me.roundaround.inventorymanagement.inventory.sorting.itemstack.ItemStackComparator.create(PLAYER_UUID);
    assertEquals(Integer.signum(baseline), Integer.signum(chain.compare(apple, diamond)),
        "Tie-break slot must not override the primary alphabetical order");
  }

  // (c) Empty registry behaves exactly like the historical no-op (returns 0).
  @Test
  void emptyRegistryIsNoOp() {
    RegistryBackedComparator slot = RegistryBackedComparator.getInstance();
    assertEquals(0, slot.compare(diamond(1), diamond(64)));
    assertEquals(0, slot.compare(new ItemStack(Items.APPLE), new ItemStack(Items.EMERALD)));
    assertEquals(0, slot.compare(new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND)));
  }

  // (d) registerKey leaves stacks its predicate does not accept untouched.
  @Test
  void registerKeyLeavesUnrecognizedItemsUntouched() {
    SortComparatorRegistry.registerKey(
        id("diamonds_by_count"), 100, (stack) -> stack.is(Items.DIAMOND), ItemStack::getCount);

    RegistryBackedComparator slot = RegistryBackedComparator.getInstance();

    // Neither operand recognized: untouched.
    assertEquals(0, slot.compare(new ItemStack(Items.EMERALD), new ItemStack(Items.APPLE)),
        "Predicate rejects both: contribution must return 0");
    // Only one operand recognized: untouched (ConditionalComparator requires both).
    assertEquals(0, slot.compare(diamond(1), new ItemStack(Items.APPLE)),
        "Predicate rejects one operand: contribution must return 0");
    // Both recognized: the key decides.
    assertTrue(slot.compare(diamond(1), diamond(8)) < 0,
        "Both diamonds recognized: lower count sorts earlier by ascending key");
  }

  // (e) Priority ordering, then registration-order tie-break at equal priority.
  @Test
  void priorityAndRegistrationOrderDecide() {
    ItemStack a = diamond(1);
    ItemStack b = diamond(8);

    // Two raw contributions for the same recognized pair, returning OPPOSITE signs.
    // Lower-priority-number (10) is consulted first under first-non-zero-wins, so it decides.
    SortComparatorRegistry.register(id("low_prio"), 10,
        (x, y) -> x.getCount() - y.getCount());   // a < b  => negative for (a, b)
    SortComparatorRegistry.register(id("high_prio"), 20,
        (x, y) -> y.getCount() - x.getCount());    // opposite sign

    assertTrue(RegistryBackedComparator.getInstance().compare(a, b) < 0,
        "Lower priority number (consulted first) must decide the order");

    // Equal priority: registration order is the tie-break (first registrant wins).
    SortComparatorRegistry.clearForTest();
    SortComparatorRegistry.register(id("first"), 50,
        (x, y) -> x.getCount() - y.getCount());   // a < b
    SortComparatorRegistry.register(id("second"), 50,
        (x, y) -> y.getCount() - x.getCount());    // opposite
    assertTrue(RegistryBackedComparator.getInstance().compare(a, b) < 0,
        "At equal priority, the first registrant must decide");

    // list() is priority-ordered ascending.
    var list = SortComparatorRegistry.list();
    assertEquals(2, list.size());
    assertEquals("first", list.get(0).id().getPath(), "Equal priority preserves registration order");
    assertEquals("second", list.get(1).id().getPath());
  }
}
