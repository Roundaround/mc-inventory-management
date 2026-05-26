package me.roundaround.inventorymanagement.testing;

import java.util.Comparator;

public class NoopComparator<T> implements Comparator<T> {
  @Override
  public int compare(T o1, T o2) {
    return 0;
  }
}
