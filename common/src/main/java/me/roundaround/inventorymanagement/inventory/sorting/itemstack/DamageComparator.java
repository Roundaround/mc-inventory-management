package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.WrapperComparatorImpl;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class DamageComparator extends WrapperComparatorImpl<ItemStack> {
  public DamageComparator() {
    super(Comparator.comparingInt(ItemStack::getDamageValue).reversed());
  }
}
