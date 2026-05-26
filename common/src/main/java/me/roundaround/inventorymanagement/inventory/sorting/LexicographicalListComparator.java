package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class LexicographicalListComparator<T, U> implements Comparator<List<T>> {
  private final Function<T, U> extractor;
  private final Comparator<U> comparator;

  protected LexicographicalListComparator(Function<T, U> extractor, Comparator<U> comparator) {
    this.extractor = extractor;
    this.comparator = comparator;
  }

  public static <T extends Comparable<? super T>> NaturalIdentity<T> naturalOrder() {
    return new NaturalIdentity<>();
  }

  public static <T, U extends Comparable<? super U>> Natural<T, U> naturalOrder(Function<T, U> extractor) {
    return new Natural<>(extractor);
  }

  public static <T> Identity<T> comparing(Comparator<T> comparator) {
    return new Identity<>(comparator);
  }

  public static <T, U> LexicographicalListComparator<T, U> comparing(
      Function<T, U> extractor, Comparator<U> comparator
  ) {
    return new LexicographicalListComparator<>(extractor, comparator);
  }

  @Override
  public int compare(List<T> o1, List<T> o2) {
    Iterator<T> iter1 = o1.iterator();
    Iterator<T> iter2 = o2.iterator();

    while (iter1.hasNext() && iter2.hasNext()) {
      U u1 = this.extractor.apply(iter1.next());
      U u2 = this.extractor.apply(iter2.next());
      int comparison = this.comparator.compare(u1, u2);
      if (comparison != 0) {
        return comparison;
      }
    }

    if (iter1.hasNext()) {
      return 1;
    } else if (iter2.hasNext()) {
      return -1;
    }

    return 0;
  }

  public static class Identity<T> extends LexicographicalListComparator<T, T> {
    public Identity(Comparator<T> comparator) {
      super(Function.identity(), comparator);
    }
  }

  public static class Natural<T, U extends Comparable<? super U>> extends LexicographicalListComparator<T, U> {
    public Natural(Function<T, U> extractor) {
      super(extractor, Comparator.naturalOrder());
    }
  }

  public static class NaturalIdentity<T extends Comparable<? super T>> extends LexicographicalListComparator<T, T> {
    public NaturalIdentity() {
      super(Function.identity(), Comparator.naturalOrder());
    }
  }
}
