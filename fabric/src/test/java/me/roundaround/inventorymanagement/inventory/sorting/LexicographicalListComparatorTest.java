package me.roundaround.inventorymanagement.inventory.sorting;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class LexicographicalListComparatorTest {
  @Nested
  class NaturalIdentity {
    private final LexicographicalListComparator.NaturalIdentity<Integer> comparator =
        LexicographicalListComparator.naturalOrder();

    @ParameterizedTest
    @MethodSource("equalForEquivalentLists_source")
    void equalForEquivalentLists(List<Integer> input) {
      assertEquals(0, comparator.compare(input, List.copyOf(input)));
    }

    private static Stream<List<Integer>> equalForEquivalentLists_source() {
      return Stream.of(List.of(1), List.of(1, 2), List.of(1, 2, 3), List.of(1, 1, 2));
    }

    @ParameterizedTest
    @MethodSource("higherValueIsHigher_source")
    void higherValueIsHigher(List<Integer> o1, List<Integer> o2, int comparison) {
      assertEquals(Math.signum(comparator.compare(o1, o2)), comparison);
    }

    private static Stream<Arguments> higherValueIsHigher_source() {
      return Stream.of(
          arguments(List.of(3), List.of(1), 1),
          arguments(List.of(2, 1), List.of(3, 1), -1)
      );
    }

    @Test
    void orderMatters() {
      List<Integer> o1 = List.of(3, 2, 1);
      List<Integer> o2 = List.of(1, 2, 3);
      assertTrue(comparator.compare(o1, o2) > 0);
    }

    @ParameterizedTest
    @MethodSource("longerIsHigher_source")
    void longerIsHigher(List<Integer> o1, List<Integer> o2, int comparison) {
      assertEquals(Math.signum(comparator.compare(o1, o2)), comparison);
    }

    private static Stream<Arguments> longerIsHigher_source() {
      return Stream.of(
          arguments(List.of(1, 2, 3), List.of(1, 2), 1),
          arguments(List.of(1, 2), List.of(1, 2, 3), -1)
      );
    }
  }

  @Nested
  class WithExtractor {
    private final LexicographicalListComparator<String, Integer> comparator =
        LexicographicalListComparator.comparing(String::length, Integer::compareTo);

    @Test
    void comparesExtractedValues() {
      List<String> o1 = List.of("aaa", "bb");
      List<String> o2 = List.of("cc", "ddd");
      assertTrue(comparator.compare(o1, o2) > 0);
    }

    @Test
    void equalWhenExtractedValuesMatch() {
      List<String> o1 = List.of("ab", "cde");
      List<String> o2 = List.of("xy", "zzz");
      assertEquals(0, comparator.compare(o1, o2));
    }
  }
}
