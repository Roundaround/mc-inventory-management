package me.roundaround.inventorymanagement.inventory.sorting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConditionalComparatorTest {
  @Test
  void delegatesWhenBothMatchCondition() {
    ConditionalComparator<Integer> comparator = ConditionalComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertTrue(comparator.compare(2, 1) > 0);
    assertTrue(comparator.compare(1, 2) < 0);
    assertEquals(0, comparator.compare(1, 1));
  }

  @Test
  void returnsZeroWhenFirstFailsCondition() {
    ConditionalComparator<Integer> comparator = ConditionalComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertEquals(0, comparator.compare(-1, 5));
  }

  @Test
  void returnsZeroWhenSecondFailsCondition() {
    ConditionalComparator<Integer> comparator = ConditionalComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertEquals(0, comparator.compare(5, -1));
  }

  @Test
  void returnsZeroWhenBothFailCondition() {
    ConditionalComparator<Integer> comparator = ConditionalComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertEquals(0, comparator.compare(-1, -2));
  }
}
