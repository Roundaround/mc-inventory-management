package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;

public abstract class WrapperComparatorImpl<T> implements WrapperComparator<T>, CachingComparator {
  private final Comparator<T> delegate;

  protected WrapperComparatorImpl(Comparator<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Comparator<T> getDelegate() {
    return this.delegate;
  }
}
