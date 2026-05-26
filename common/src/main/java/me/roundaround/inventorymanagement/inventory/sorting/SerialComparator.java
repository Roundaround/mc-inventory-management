package me.roundaround.inventorymanagement.inventory.sorting;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public interface SerialComparator<T> extends Comparator<T>, Iterable<Comparator<T>>, CachingComparator {
  @Override
  default int compare(T o1, T o2) {
    for (Comparator<T> comparator : this) {
      int result = comparator.compare(o1, o2);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  @Override
  default void clearCache() {
    for (Comparator<T> comparator : this) {
      if (comparator instanceof CachingComparator caching) {
        caching.clearCache();
      }
    }
  }

  @SafeVarargs
  static <T> SerialComparator<T> comparing(
      Comparator<T>... delegates
  ) {
    return comparing(List.of(delegates));
  }

  static <T> SerialComparator<T> comparing(
      Collection<Comparator<T>> delegates
  ) {
    return new SerialComparator<>() {
      @Override
      public @NotNull Iterator<Comparator<T>> iterator() {
        return delegates.iterator();
      }
    };
  }
}
