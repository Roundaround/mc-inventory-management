package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

public interface PredicatedComparator<T> extends WrapperComparator<T> {
  Predicate<T> getPredicate();

  Comparator<T> getPassDelegate();

  @Override
  default Comparator<T> getDelegate() {
    return (o1, o2) -> {
      if (!this.getPredicate().test(o1) || !this.getPredicate().test(o2)) {
        return 0;
      }
      return this.getPassDelegate().compare(o1, o2);
    };
  }

  static <T> PredicatedComparator<T> of(Predicate<T> predicate, Comparator<T> delegate) {
    return new PredicatedComparator<>() {
      @Override
      public Predicate<T> getPredicate() {
        return predicate;
      }

      @Override
      public Comparator<T> getPassDelegate() {
        return delegate;
      }
    };
  }

  static <T extends Comparable<? super T>> PredicatedComparator<T> naturalOrder(Predicate<T> predicate) {
    return of(predicate, Comparator.naturalOrder());
  }

  static <T extends Comparable<? super T>> PredicatedComparator<T> reverseOrder(Predicate<T> predicate) {
    return of(predicate, Comparator.reverseOrder());
  }

  static <T> PredicatedComparator<T> ignoreNulls(Comparator<T> delegate) {
    return of(Objects::nonNull, delegate);
  }

  static <T extends Comparable<? super T>> PredicatedComparator<T> ignoreNullsNaturalOrder() {
    return of((T t) -> t != null, Comparator.naturalOrder());
  }

  static <T extends Comparable<? super T>> PredicatedComparator<T> ignoreNullsReverseOrder() {
    return of((T t) -> t != null, Comparator.reverseOrder());
  }
}
