package me.roundaround.inventorymanagement.inventory.sorting;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachingComparatorImplTest {
  @Test
  void cachesMappedValueOnlyOncePerElement() {
    WrappedCachingComparatorImpl<Element> comparator = new WrappedCachingComparatorImpl<>();
    genSample(100).sort(comparator);
    assertEquals(100, comparator.getComputeCount());
  }

  @Test
  void cachesAcrossExecutions() {
    ArrayList<Element> sample = genSample(100);
    WrappedCachingComparatorImpl<Element> comparator = new WrappedCachingComparatorImpl<>();
    sample.sort(comparator);
    sample.sort(comparator);
    assertEquals(100, comparator.getComputeCount());
  }

  @Test
  void recalculatesMappedValuesAfterClear() {
    ArrayList<Element> sample = genSample(100);
    WrappedCachingComparatorImpl<Element> comparator = new WrappedCachingComparatorImpl<>();
    sample.sort(comparator);
    comparator.clearCache();
    sample.sort(comparator);
    assertEquals(200, comparator.getComputeCount());
  }

  private static ArrayList<Element> genSample(int size) {
    ArrayList<Element> sample = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      sample.add(new Element((int) Math.round(Math.random() * 100)));
    }
    return sample;
  }

  private record Element(int value) {
    @Override
    public String toString() {
      return "Element{value=" + value + '}';
    }
  }

  private static class WrappedCachingComparatorImpl<O> extends CachingComparatorImpl<O, String> {
    private int computeCount = 0;

    public WrappedCachingComparatorImpl() {
      super(String::compareTo);
    }

    @Override
    protected String mapValue(O o) {
      this.computeCount++;
      return o.toString();
    }

    public int getComputeCount() {
      return this.computeCount;
    }
  }
}
