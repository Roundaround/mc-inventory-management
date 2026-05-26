package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.WrapperComparatorImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Comparator;

public class PlayerHeadNameComparator extends WrapperComparatorImpl<ItemStack> {
  public PlayerHeadNameComparator() {
    super(Comparator.comparing(PlayerHeadNameComparator::getPlayerHeadName,
        Comparator.nullsLast(String::compareToIgnoreCase)
    ));
  }

  private static String getPlayerHeadName(ItemStack stack) {
    ResolvableProfile profile = stack.get(DataComponents.PROFILE);
    return profile == null || profile.name().isEmpty() ? null : profile.name().get();
  }
}
