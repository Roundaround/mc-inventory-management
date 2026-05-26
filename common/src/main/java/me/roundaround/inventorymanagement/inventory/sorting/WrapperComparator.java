package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;

public interface WrapperComparator<T> extends Comparator<T>, CachingComparator {
  Comparator<T> getDelegate();

  @Override
  default int compare(T o1, T o2) {
    return this.getDelegate().compare(o1, o2);
  }

  @Override
  default void clearCache() {
    if (this.getDelegate() instanceof CachingComparator caching) {
      caching.clearCache();
    }
  }
}
