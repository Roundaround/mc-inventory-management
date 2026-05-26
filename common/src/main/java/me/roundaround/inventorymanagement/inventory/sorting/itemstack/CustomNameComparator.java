package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.WrapperComparatorImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class CustomNameComparator extends WrapperComparatorImpl<ItemStack> {
  public CustomNameComparator() {
    super(Comparator.comparing(CustomNameComparator::getCustomName, Comparator.nullsLast(String::compareToIgnoreCase)));
  }

  private static String getCustomName(ItemStack stack) {
    Component customName = stack.get(DataComponents.CUSTOM_NAME);
    return customName == null ? null : customName.getString();
  }
}
