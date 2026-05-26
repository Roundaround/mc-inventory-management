package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;

public class NoOpComparator<T> implements Comparator<T> {
  @Override
  public int compare(T o1, T o2) {
    return 0;
  }
}
