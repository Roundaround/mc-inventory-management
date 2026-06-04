package me.roundaround.inventorymanagement.inventory;

import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

/**
 * Unit tests for {@link SortableInventory#sort(SlotRange, Comparator)}, the pure sort engine behind
 * {@code ClientInventoryHelper.calculateSort} (which is just
 * {@code new SortableInventory(inventory).sort(slotRange, ItemStackComparator.create(uuid))}).
 *
 * <p>A simple, deterministic comparator on {@code getDescriptionId()} ({@link #BY_NAME}) isolates the
 * engine's projection / empty-handling / ordering behavior without depending on the full
 * {@code ItemStackComparator} (which has its own coverage under
 * {@code inventory/sorting/itemstack/}). All candidate items below have an
 * {@code item.minecraft.*} description id so the lexical key order is intuitive and bootstrap-stable.
 *
 * <p>{@code sort} returns a list of ORIGINAL slot indices in sorted order, projected back into the
 * slot range via {@link SlotRange#project}; the result length is the range's {@code max} (not its
 * size), with {@code -1} in empty / out-of-range positions.
 *
 * <p>{@code ServerInventoryHelper.applySort} is intentionally not covered here: every public
 * entrypoint requires a real {@code Player} (it calls {@code player.getInventory()} /
 * {@code getContainerInventory(player)}), so it is out of unit-test scope and is exercised in-game
 * rather than refactored solely to make it testable.
 */
public class SortableInventoryTest extends BaseMinecraftTest {
  private static final Comparator<ItemStack> BY_NAME =
      Comparator.comparing((stack) -> stack.getItem().getDescriptionId());
  private static final Comparator<ItemStack> BY_COUNT =
      Comparator.comparingInt(ItemStack::getCount);

  @Test
  void sortsByComparator_fullRange() {
    SimpleContainer container = new SimpleContainer(3);
    container.setItem(0, new ItemStack(Items.DIAMOND));
    container.setItem(1, new ItemStack(Items.APPLE));
    container.setItem(2, new ItemStack(Items.COAL));

    // descriptionId order: apple < coal < diamond -> original slots 1, 2, 0.
    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.fullRange(container), BY_NAME);

    assertIterableEquals(List.of(1, 2, 0), sorted);
  }

  @Test
  void filtersEmptySlots() {
    SimpleContainer container = new SimpleContainer(5);
    container.setItem(0, new ItemStack(Items.DIAMOND));
    container.setItem(2, new ItemStack(Items.COAL));
    container.setItem(4, new ItemStack(Items.APPLE));
    // Slots 1 and 3 stay empty.

    // Non-empty stacks pack to the front in sorted order (apple < coal < diamond -> slots 4, 2, 0),
    // trailing positions pad with -1 to the full range length (max = 5).
    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.fullRange(container), BY_NAME);

    assertIterableEquals(List.of(4, 2, 0, -1, -1), sorted);
  }

  @Test
  void allEmptyRange() {
    SimpleContainer container = new SimpleContainer(4);

    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.fullRange(container), BY_NAME);

    assertIterableEquals(List.of(-1, -1, -1, -1), sorted);
  }

  @Test
  void projectsWithinSubRange() {
    SimpleContainer container = new SimpleContainer(6);
    // In range (slots 1..3 for bySize(1, 3)).
    container.setItem(1, new ItemStack(Items.EMERALD));
    container.setItem(2, new ItemStack(Items.APPLE));
    container.setItem(3, new ItemStack(Items.STICK));
    // Out of range: must be ignored entirely.
    container.setItem(0, new ItemStack(Items.DIAMOND));
    container.setItem(4, new ItemStack(Items.COAL));
    container.setItem(5, new ItemStack(Items.BREAD));

    // bySize(1, 3) -> min 1, max 4, slots {1, 2, 3}; projection length is max = 4 with a leading -1.
    // descriptionId order of the in-range items: apple(2) < emerald(1) < stick(3).
    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.bySize(1, 3), BY_NAME);

    assertIterableEquals(List.of(-1, 2, 1, 3), sorted);
  }

  @Test
  void alreadySorted_isIdentity() {
    SimpleContainer container = new SimpleContainer(3);
    // Items already in BY_NAME order: apple < coal < diamond.
    container.setItem(0, new ItemStack(Items.APPLE));
    container.setItem(1, new ItemStack(Items.COAL));
    container.setItem(2, new ItemStack(Items.DIAMOND));

    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.fullRange(container), BY_NAME);

    assertIterableEquals(List.of(0, 1, 2), sorted);
  }

  @Test
  void reverseSorted_isReversedIndices() {
    SimpleContainer container = new SimpleContainer(3);
    // Items in reverse BY_NAME order: diamond > coal > apple.
    container.setItem(0, new ItemStack(Items.DIAMOND));
    container.setItem(1, new ItemStack(Items.COAL));
    container.setItem(2, new ItemStack(Items.APPLE));

    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.fullRange(container), BY_NAME);

    assertIterableEquals(List.of(2, 1, 0), sorted);
  }

  @Test
  void stableForEqualKeys() {
    SimpleContainer container = new SimpleContainer(3);
    // Two distinct items tied under BY_COUNT (count 5) in slots 0 and 2, plus a smaller-count item
    // in slot 1. Stream.sorted is stable, so the tied pair must keep ascending original-slot order.
    container.setItem(0, new ItemStack(Items.DIAMOND, 5));
    container.setItem(1, new ItemStack(Items.APPLE, 1));
    container.setItem(2, new ItemStack(Items.COAL, 5));

    // Ascending by count: apple(1) first, then the count-5 tie preserves slot order 0 before 2.
    List<Integer> sorted = new SortableInventory(container).sort(SlotRange.fullRange(container), BY_COUNT);

    assertIterableEquals(List.of(1, 0, 2), sorted);
  }

  @Test
  void sortReturnsMaxLengthList() {
    // Documents the projection-length contract: result length equals SlotRange.max(), not size().
    SimpleContainer container = new SimpleContainer(4);
    container.setItem(0, new ItemStack(Items.APPLE));
    container.setItem(1, new ItemStack(Items.DIAMOND));

    SlotRange range = SlotRange.bySize(1, 2);
    List<Integer> sorted = new SortableInventory(container).sort(range, BY_NAME);

    assertEquals(range.max(), sorted.size());
  }
}
