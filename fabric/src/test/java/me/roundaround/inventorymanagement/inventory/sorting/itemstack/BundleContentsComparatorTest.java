package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BundleContentsComparatorTest extends BaseMinecraftTest {
  private static BundleContentsComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new BundleContentsComparator();
  }

  @Test
  void ignoresNonBundleItems() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void sortsDescendingByItemCount() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createBundle(List.of(
            new ItemStack(Items.DIAMOND, 1),
            new ItemStack(Items.DIRT, 1),
            new ItemStack(Items.STONE, 1))),
        createBundle(List.of(
            new ItemStack(Items.DIAMOND, 1),
            new ItemStack(Items.DIRT, 1))),
        createBundle(List.of(
            new ItemStack(Items.DIAMOND, 1)))
    ));
  }

  @Test
  void sortsDescendingByTotalQuantity() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createBundle(List.of(new ItemStack(Items.DIAMOND, 32))),
        createBundle(List.of(new ItemStack(Items.DIAMOND, 16))),
        createBundle(List.of(new ItemStack(Items.DIAMOND, 1)))
    ));
  }

  @Test
  void emptyBundlesAreEqual() {
    assertEquals(0, comparator.compare(
        createBundle(List.of()),
        createBundle(List.of())
    ));
  }

  private static ItemStack createBundle(List<ItemStack> items) {
    ItemStack stack = new ItemStack(Items.BUNDLE);
    BundleContents.Mutable mutable = new BundleContents.Mutable(BundleContents.EMPTY);
    for (ItemStack item : items) {
      mutable.tryInsert(item);
    }
    stack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
    return stack;
  }
}
