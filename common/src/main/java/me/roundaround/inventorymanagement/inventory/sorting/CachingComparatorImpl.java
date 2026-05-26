package me.roundaround.inventorymanagement.inventory.sorting;

import java.util.Comparator;
import java.util.IdentityHashMap;

public abstract class CachingComparatorImpl<T, O> implements Comparator<T>, CachingComparator {
  private final Comparator<O> delegate;
  private final ThreadLocal<IdentityHashMap<T, O>> cache = ThreadLocal.withInitial(IdentityHashMap::new);

  protected CachingComparatorImpl(Comparator<O> delegate) {
    this.delegate = delegate;
  }

  protected abstract O mapValue(T t);

  @Override
  public final int compare(T t1, T t2) {
    IdentityHashMap<T, O> localCache = this.cache.get();
    O o1 = localCache.computeIfAbsent(t1, this::mapValue);
    O o2 = localCache.computeIfAbsent(t2, this::mapValue);
    return this.delegate.compare(o1, o2);
  }

  @Override
  public void clearCache() {
    this.cache.remove();
  }

  public final Comparator<O> getDelegate() {
    return this.delegate;
  }
}
