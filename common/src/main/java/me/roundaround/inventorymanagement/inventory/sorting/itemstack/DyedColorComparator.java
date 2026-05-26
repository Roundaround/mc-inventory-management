package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Arrays;
import java.util.Comparator;

public class DyedColorComparator extends CachingComparatorImpl<ItemStack, Integer> {
  public DyedColorComparator() {
    super(Comparator.naturalOrder());
  }

  @Override
  protected Integer mapValue(ItemStack stack) {
    DyedItemColor component = stack.get(DataComponents.DYED_COLOR);
    if (component != null) {
      return component.rgb();
    }

    String itemString = stack.getItem().toString();
    return Arrays.stream(DyeColor.values())
        .filter(dyeColor -> itemString.startsWith(dyeColor.getName()))
        .mapToInt(DyeColor::getId)
        .map(i -> i + 1)
        .findFirst()
        .orElse(0);
  }
}
