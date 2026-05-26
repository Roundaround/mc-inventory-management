package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class BundleContentsComparator extends CachingComparatorImpl<ItemStack,
    BundleContentsComparator.BundleSummary> {
  public BundleContentsComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected BundleSummary mapValue(ItemStack stack) {
    return BundleSummary.of(stack);
  }

  protected record BundleSummary(int itemCount, int totalQuantity) implements Comparable<BundleSummary> {
    private static Comparator<BundleSummary> comparator;

    public static BundleSummary of(ItemStack stack) {
      BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
      if (contents == null) {
        return null;
      }

      var itemCount = new Object() { int value = 0; };
      var totalQuantity = new Object() { int value = 0; };

      contents.itemCopyStream().forEach((slotStack) -> {
        itemCount.value++;
        totalQuantity.value += slotStack.getCount();
      });

      return new BundleSummary(itemCount.value, totalQuantity.value);
    }

    @Override
    public int compareTo(@NotNull BundleSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<BundleSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(BundleSummary::itemCount).reversed(),
            Comparator.comparingInt(BundleSummary::totalQuantity).reversed()
        );
      }
      return comparator;
    }
  }
}
