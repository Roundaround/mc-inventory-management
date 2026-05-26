package me.roundaround.inventorymanagement.inventory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SlotRangeTest {
  @Test
  void projectWhenPerfectMatch() {
    SlotRange slotRange = SlotRange.bySize(0, 3);
    List<Integer> values = List.of(1, 2, 3);

    List<Integer> expected = List.of(1, 2, 3);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenValuesSmaller() {
    SlotRange slotRange = SlotRange.bySize(0, 5);
    List<Integer> values = List.of(1, 2, 3);

    List<Integer> expected = List.of(1, 2, 3, -1, -1);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenValuesLarger() {
    SlotRange slotRange = SlotRange.bySize(0, 3);
    List<Integer> values = List.of(1, 2, 3, 4, 5);

    List<Integer> expected = List.of(1, 2, 3);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenMinNonZero() {
    SlotRange slotRange = SlotRange.bySize(1, 5);
    List<Integer> values = List.of(1, 2, 3);

    List<Integer> expected = List.of(-1, 1, 2, 3, -1, -1);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenMinNonZeroAndValuesLarger() {
    SlotRange slotRange = SlotRange.bySize(1, 3);
    List<Integer> values = List.of(1, 2, 3, 4, 5);

    List<Integer> expected = List.of(-1, 1, 2, 3);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenRangeIsEmpty() {
    SlotRange slotRange = SlotRange.empty();
    List<Integer> values = List.of(1, 2, 3);

    List<Integer> expected = List.of();
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenRangeHasExclusions() {
    SlotRange slotRange = SlotRange.bySize(0, 5).withExclusions(List.of(2));
    List<Integer> values = List.of(1, 2, 3);

    List<Integer> expected = List.of(1, 2, -1, 3, -1);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void projectWhenEntireRangeIsExcluded() {
    SlotRange slotRange = SlotRange.bySize(0, 3).withExclusions(List.of(0, 1, 2));
    List<Integer> values = List.of(1, 2, 3);

    List<Integer> expected = List.of(-1, -1, -1);
    assertIterableEquals(expected, slotRange.project(values, -1));
  }

  @Test
  void sizeReflectsExclusions() {
    SlotRange slotRange = SlotRange.byMax(0, 10).withExclusions(List.of(3, 5, 7));
    assertEquals(7, slotRange.size());
  }

  @Test
  void containsRespectsExclusions() {
    SlotRange slotRange = SlotRange.byMax(0, 5).withExclusions(List.of(2));
    assertTrue(slotRange.contains(0));
    assertTrue(slotRange.contains(1));
    assertFalse(slotRange.contains(2));
    assertTrue(slotRange.contains(3));
    assertTrue(slotRange.contains(4));
    assertFalse(slotRange.contains(5));
  }

  @Test
  void getSlotsExcludesExcluded() {
    SlotRange slotRange = SlotRange.byMax(0, 5).withExclusions(List.of(1, 3));
    assertIterableEquals(List.of(0, 2, 4), slotRange.getSlots());
  }

  @Test
  void byMaxAndBySizeEquivalent() {
    SlotRange byMax = SlotRange.byMax(2, 7);
    SlotRange bySize = SlotRange.bySize(2, 5);
    assertEquals(byMax.min(), bySize.min());
    assertEquals(byMax.max(), bySize.max());
    assertEquals(byMax.size(), bySize.size());
    assertIterableEquals(byMax.getSlots(), bySize.getSlots());
  }

  @Test
  void maxLessThanMinClampsToEmpty() {
    SlotRange slotRange = new SlotRange(5, 3);
    assertEquals(0, slotRange.size());
    assertTrue(slotRange.getSlots().isEmpty());
  }
}
