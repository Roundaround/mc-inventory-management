package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrittenBookComparatorTest extends BaseMinecraftTest {
  private static WrittenBookComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new WrittenBookComparator();
  }

  @Test
  void ignoresNonBookItems() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void sortsByAuthorThenTitle() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createBook("Alice", "Adventure"),
        createBook("Alice", "Zebra"),
        createBook("Bob", "Adventure")
    ));
  }

  @Test
  void sortsByGenerationAscending() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createBook("Author", "Title", 0),
        createBook("Author", "Title", 1),
        createBook("Author", "Title", 2)
    ));
  }

  @Test
  void sameBooksAreEqual() {
    assertEquals(0, comparator.compare(
        createBook("Author", "Title"),
        createBook("Author", "Title")
    ));
  }

  private static ItemStack createBook(String author, String title) {
    return createBook(author, title, 0);
  }

  private static ItemStack createBook(String author, String title, int generation) {
    ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
    stack.set(DataComponents.WRITTEN_BOOK_CONTENT,
        new WrittenBookContent(Filterable.passThrough(title), author, generation, List.of(), true));
    return stack;
  }
}
