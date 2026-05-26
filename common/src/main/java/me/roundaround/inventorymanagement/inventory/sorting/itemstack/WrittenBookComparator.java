package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class WrittenBookComparator extends CachingComparatorImpl<ItemStack, WrittenBookComparator.BookSummary> {
  public WrittenBookComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected BookSummary mapValue(ItemStack stack) {
    return BookSummary.of(stack);
  }

  protected record BookSummary(String author, String title, int generation) implements Comparable<BookSummary> {
    private static Comparator<BookSummary> comparator;

    public static BookSummary of(ItemStack stack) {
      WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
      if (content == null) {
        return null;
      }

      return new BookSummary(
          content.author(),
          content.title().raw(),
          content.generation()
      );
    }

    @Override
    public int compareTo(@NotNull BookSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<BookSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparing(BookSummary::author, String::compareToIgnoreCase),
            Comparator.comparing(BookSummary::title, String::compareToIgnoreCase),
            Comparator.comparingInt(BookSummary::generation)
        );
      }
      return comparator;
    }
  }
}
