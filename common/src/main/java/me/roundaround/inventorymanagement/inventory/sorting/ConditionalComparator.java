package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;
import java.util.function.Predicate;

public class ConditionalComparator<T> implements Comparator<T> {
  private final Predicate<? super T> condition;
  private final Comparator<T> baseComparator;

  private ConditionalComparator(Predicate<? super T> condition, Comparator<T> baseComparator) {
    this.condition = condition;
    this.baseComparator = baseComparator;
  }

  @Override
  public int compare(T o1, T o2) {
    if (!this.condition.test(o1) || !this.condition.test(o2)) {
      return 0;
    }
    return this.baseComparator.compare(o1, o2);
  }

  public static <T> ConditionalComparator<T> comparing(Predicate<T> condition, Comparator<T> baseComparator) {
    return new ConditionalComparator<>(condition, baseComparator);
  }
}
