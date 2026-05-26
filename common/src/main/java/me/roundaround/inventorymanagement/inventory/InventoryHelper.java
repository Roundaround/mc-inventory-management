package me.roundaround.inventorymanagement.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class InventoryHelper {
  public static boolean canTakeItemFromSlot(AbstractContainerMenu screenHandler, int idx, Player player) {
    if (screenHandler == null) {
      return true;
    }
    try {
      return screenHandler.getSlot(idx).mayPickup(player);
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }

  public static boolean canPlaceItemInSlot(AbstractContainerMenu screenHandler, int idx, ItemStack itemStack) {
    if (screenHandler == null) {
      return true;
    }
    try {
      return screenHandler.getSlot(idx).mayPlace(itemStack);
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }

  public static Container getContainerInventory(Player player) {
    AbstractContainerMenu currentScreenHandler = player.containerMenu;
    if (currentScreenHandler == null) {
      return null;
    }

    try {
      return currentScreenHandler.getSlot(0).container;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public static boolean canStacksBeMerged(ItemStack toStack, ItemStack fromStack) {
    return !toStack.isEmpty() && ItemStack.isSameItemSameComponents(toStack, fromStack) &&
           toStack.isStackable() && toStack.getCount() < toStack.getMaxStackSize();
  }

  public static boolean mergeStacks(ItemStack toStack, ItemStack fromStack) {
    int space = toStack.getMaxStackSize() - toStack.getCount();
    int amount = Math.min(space, fromStack.getCount());
    if (amount > 0) {
      toStack.grow(amount);
      fromStack.shrink(amount);
      return true;
    }
    return false;
  }
}
