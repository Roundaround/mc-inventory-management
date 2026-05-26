package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class ContainerFirstComparator extends CachingComparatorImpl<ItemStack, Boolean> {
  public ContainerFirstComparator() {
    super(Comparator.reverseOrder());
  }

  @Override
  protected Boolean mapValue(ItemStack stack) {
    return stack.has(DataComponents.CONTAINER);
  }
}
