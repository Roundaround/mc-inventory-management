package me.roundaround.inventorymanagement.inventory.sorting;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SerialComparatorTest {
  @Test
  void returnsFirstNonZeroResult() {
    SerialComparator<int[]> comparator = SerialComparator.comparing(
        Comparator.comparingInt(a -> a[0]),
        Comparator.comparingInt(a -> a[1])
    );

    assertTrue(comparator.compare(new int[]{2, 0}, new int[]{1, 0}) > 0);
    assertTrue(comparator.compare(new int[]{1, 0}, new int[]{2, 0}) < 0);
  }

  @Test
  void fallsThroughToNextComparator() {
    SerialComparator<int[]> comparator = SerialComparator.comparing(
        Comparator.comparingInt(a -> a[0]),
        Comparator.comparingInt(a -> a[1])
    );

    assertTrue(comparator.compare(new int[]{1, 2}, new int[]{1, 1}) > 0);
    assertTrue(comparator.compare(new int[]{1, 1}, new int[]{1, 2}) < 0);
  }

  @Test
  void returnsZeroWhenAllEqual() {
    SerialComparator<int[]> comparator = SerialComparator.comparing(
        Comparator.comparingInt(a -> a[0]),
        Comparator.comparingInt(a -> a[1])
    );

    assertEquals(0, comparator.compare(new int[]{1, 2}, new int[]{1, 2}));
  }

  @Test
  void clearsChildCaches() {
    TrackingCachingComparator tracker = new TrackingCachingComparator();
    SerialComparator<String> comparator = SerialComparator.comparing(tracker);

    assertFalse(tracker.cleared);
    comparator.clearCache();
    assertTrue(tracker.cleared);
  }

  @Test
  void sortsListCorrectly() {
    SerialComparator<String> comparator = SerialComparator.comparing(
        Comparator.comparingInt(String::length),
        Comparator.naturalOrder()
    );

    ArrayList<String> list = new ArrayList<>(List.of("bb", "aaa", "a", "cc", "bbb"));
    list.sort(comparator);

    assertIterableEquals(List.of("a", "bb", "cc", "aaa", "bbb"), list);
  }

  private static class TrackingCachingComparator implements Comparator<String>, CachingComparator {
    boolean cleared = false;

    @Override
    public int compare(String o1, String o2) {
      return 0;
    }

    @Override
    public void clearCache() {
      this.cleared = true;
    }
  }
}
