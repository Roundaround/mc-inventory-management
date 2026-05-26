package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;
import java.util.function.Predicate;

public class ConditionalComparator<T> implements Comparator<T> {
  private final Predicate<T> predicate;
  private final Comparator<T> delegate;

  protected ConditionalComparator(Predicate<T> predicate, Comparator<T> delegate) {
    this.predicate = predicate;
    this.delegate = delegate;
  }

  @Override
  public int compare(T o1, T o2) {
    if (!this.predicate.test(o1) || !this.predicate.test(o2)) {
      return 0;
    }
    return this.delegate.compare(o1, o2);
  }

  public static <T> ConditionalComparator<T> of(Predicate<T> predicate, Comparator<T> base) {
    return new ConditionalComparator<>(predicate, base);
  }
}
