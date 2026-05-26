package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class ContainerContentsComparator extends CachingComparatorImpl<ItemStack,
    ContainerContentsComparator.ContentsSummary> {
  public ContainerContentsComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected ContentsSummary mapValue(ItemStack stack) {
    return ContentsSummary.of(stack);
  }

  protected record ContentsSummary(int usedSlots, int totalCount) implements Comparable<ContentsSummary> {
    private static Comparator<ContentsSummary> comparator;

    public static ContentsSummary of(ItemStack stack) {
      ItemContainerContents component = stack.get(DataComponents.CONTAINER);
      if (component == null) {
        return null;
      }

      var usedSlots = new Object() {
        int value = 0;
      };
      var totalCount = new Object() {
        int value = 0;
      };
      component.nonEmptyItemCopyStream().forEach((slotStack) -> {
        usedSlots.value++;
        totalCount.value += slotStack.getCount();
      });

      return new ContentsSummary(usedSlots.value, totalCount.value);
    }

    @Override
    public int compareTo(@NotNull ContentsSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<ContentsSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(ContentsSummary::usedSlots).reversed(),
            Comparator.comparingInt(ContentsSummary::totalCount).reversed()
        );
      }
      return comparator;
    }
  }
}
