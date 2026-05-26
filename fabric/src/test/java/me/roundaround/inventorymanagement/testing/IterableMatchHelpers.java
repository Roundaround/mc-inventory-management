package me.roundaround.inventorymanagement.testing;

import com.google.common.collect.Lists;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.IntStream;

import static me.roundaround.inventorymanagement.testing.DataGen.nameStacks;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class IterableMatchHelpers {
  private IterableMatchHelpers() {
  }

  public static <T> void assertIterableMatches(
      Iterable<T> expected, Iterable<T> actual
  ) {
    assertIterableMatches(expected, actual, Objects::equals, Object::toString);
  }

  public static <T> void assertIterableMatches(
      Iterable<T> expected, Iterable<T> actual, BiPredicate<T, T> predicate, Function<T, String> toString
  ) {
    assertIterableMatches(expected, actual, predicate, Function.identity(), Function.identity(), toString, toString);
  }

  public static <E, A, T> void assertIterableMatches(
      Iterable<E> expected, Iterable<A> actual, Function<E, T> expectedSelector, Function<A, T> actualSelector
  ) {
    assertIterableMatches(expected, actual, expectedSelector, actualSelector, Object::toString, Object::toString);
  }

  public static <E, A, T> void assertIterableMatches(
      Iterable<E> expected,
      Iterable<A> actual,
      Function<E, T> expectedSelector,
      Function<A, T> actualSelector,
      Function<E, String> expectedToString,
      Function<A, String> actualToString
  ) {
    assertIterableMatches(expected, actual, Objects::equals, expectedSelector, actualSelector,
        expectedToString, actualToString);
  }

  public static <E, A, T> void assertIterableMatches(
      Iterable<E> expected,
      Iterable<A> actual,
      BiPredicate<T, T> predicate,
      Function<E, T> expectedSelector,
      Function<A, T> actualSelector,
      Function<E, String> expectedToString,
      Function<A, String> actualToString
  ) {
    Iterator<E> expectedIterator = expected.iterator();
    Iterator<A> actualIterator = actual.iterator();

    int index = 0;
    while (expectedIterator.hasNext() && actualIterator.hasNext()) {
      E expectedElement = expectedIterator.next();
      A actualElement = actualIterator.next();

      T expectedSelected = expectedSelector.apply(expectedElement);
      T actualSelected = actualSelector.apply(actualElement);

      if (!predicate.test(expectedSelected, actualSelected)) {
        fail(String.format(
            "Elements at index %d are not equal according to the provided predicate: expected=%s, actual=%s",
            index,
            expectedToString.apply(expectedElement),
            actualToString.apply(actualElement)
        ));
      }
      index++;
    }

    if (expectedIterator.hasNext() || actualIterator.hasNext()) {
      fail("Iterables are of different lengths.");
    }
  }

  public static void assertNamesInOrder(List<ItemStack> stacks) {
    List<String> expected = IntStream.range(1, stacks.size() + 1).boxed().map((id) -> Integer.toString(id)).toList();
    assertIterableEquals(expected, selectNames(stacks));
  }

  public static void assertPreservesOrder(Comparator<ItemStack> comparator, ArrayList<ItemStack> stacks) {
    List<ItemStack> actual = nameStacks(ensureMutable(stacks));
    Collections.shuffle(actual);
    actual.sort(comparator);
    assertNamesInOrder(actual);
  }

  public static <T, U> List<U> select(List<T> source, Function<T, U> map) {
    return source.stream().map(map).toList();
  }

  public static List<String> selectNames(List<ItemStack> source) {
    return select(source, (stack) -> Objects.requireNonNull(stack.get(DataComponents.CUSTOM_NAME)).getString());
  }

  public static List<Integer> selectCounts(List<ItemStack> source) {
    return select(source, ItemStack::getCount);
  }

  private static <T> ArrayList<T> ensureMutable(List<T> input) {
    return input instanceof ArrayList<T> mutable ? mutable : Lists.newArrayList(input);
  }
}
