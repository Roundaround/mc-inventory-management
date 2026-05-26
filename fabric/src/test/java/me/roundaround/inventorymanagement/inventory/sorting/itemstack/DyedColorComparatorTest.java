package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DyedColorComparatorTest extends BaseMinecraftTest {
  private static DyedColorComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new DyedColorComparator();
  }

  @Test
  void uncoloredItemsAreEqual() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void sortsByDyedRgbAscending() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createDyed(0x000000),
        createDyed(0x800000),
        createDyed(0xFF0000),
        createDyed(0xFFFFFF)
    ));
  }

  @Test
  void dyedItemSortsAfterUndyed() {
    ItemStack undyed = new ItemStack(Items.DIAMOND);
    ItemStack dyed = createDyed(0xFF0000);
    assertTrue(comparator.compare(undyed, dyed) < 0);
  }

  private static ItemStack createDyed(int rgb) {
    ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb));
    return stack;
  }
}
