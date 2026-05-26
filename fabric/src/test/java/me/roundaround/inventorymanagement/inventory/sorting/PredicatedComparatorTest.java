package me.roundaround.inventorymanagement.inventory.sorting;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class PredicatedComparatorTest {
  @Test
  void delegatesWhenBothMatchPredicate() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertTrue(comparator.compare(2, 1) > 0);
    assertTrue(comparator.compare(1, 2) < 0);
  }

  @Test
  void returnsZeroWhenFirstFailsPredicate() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertEquals(0, comparator.compare(-1, 5));
  }

  @Test
  void returnsZeroWhenSecondFailsPredicate() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertEquals(0, comparator.compare(5, -1));
  }

  @Test
  void returnsZeroWhenBothFailPredicate() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.of(
        i -> i > 0,
        Integer::compareTo
    );

    assertEquals(0, comparator.compare(-1, -2));
  }

  @Test
  void naturalOrderComparesByNaturalOrdering() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.naturalOrder(i -> i != null);
    assertTrue(comparator.compare(1, 2) < 0);
    assertTrue(comparator.compare(2, 1) > 0);
    assertEquals(0, comparator.compare(3, 3));
  }

  @Test
  void reverseOrderComparesInReverse() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.reverseOrder(i -> i != null);
    assertTrue(comparator.compare(1, 2) > 0);
    assertTrue(comparator.compare(2, 1) < 0);
  }

  @Test
  void ignoreNullsSkipsNullElements() {
    PredicatedComparator<String> comparator = PredicatedComparator.ignoreNulls(String::compareTo);
    assertEquals(0, comparator.compare(null, "a"));
    assertEquals(0, comparator.compare("a", null));
    assertEquals(0, comparator.compare(null, null));
    assertTrue(comparator.compare("b", "a") > 0);
  }

  @Test
  void ignoreNullsNaturalOrderWorks() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.<Integer>ignoreNullsNaturalOrder();
    assertEquals(0, comparator.compare(null, 1));
    assertTrue(comparator.compare(2, 1) > 0);
  }

  @Test
  void sortingWithPredicateOrdersMatchingElements() {
    PredicatedComparator<Integer> comparator = PredicatedComparator.<Integer>of(
        i -> i >= 0,
        Comparator.naturalOrder()
    );

    ArrayList<Integer> positives = new ArrayList<>(List.of(3, 1, 2));
    positives.sort(comparator);

    assertIterableEquals(List.of(1, 2, 3), positives);
  }
}
