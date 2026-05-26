package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.OminousBottleAmplifier;

import java.util.Comparator;

public class OminousBottleComparator extends CachingComparatorImpl<ItemStack, Integer> {
  public OminousBottleComparator() {
    super(PredicatedComparator.ignoreNulls(Comparator.<Integer>reverseOrder()));
  }

  @Override
  protected Integer mapValue(ItemStack stack) {
    OminousBottleAmplifier amplifier = stack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
    if (amplifier == null) {
      return null;
    }
    return amplifier.value();
  }
}
