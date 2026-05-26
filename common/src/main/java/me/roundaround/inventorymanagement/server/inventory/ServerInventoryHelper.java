package me.roundaround.inventorymanagement.server.inventory;

import me.roundaround.inventorymanagement.inventory.SlotRange;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import static me.roundaround.inventorymanagement.inventory.InventoryHelper.*;

public final class ServerInventoryHelper {
  private ServerInventoryHelper() {
  }

  public static void applySort(Player player, boolean isPlayerInventory, List<Integer> sorted) {
    if (isPlayerInventory) {
      applyPlayerSort(player, sorted);
    } else {
      applyContainerSort(player, sorted);
    }
  }

  public static void applyPlayerSort(Player player, List<Integer> sorted) {
    Container inventory = player.getInventory();
    SlotRange slotRange = SlotRange.playerMainRange();
    applySort(player, inventory, slotRange, sorted);
  }

  public static void applyContainerSort(Player player, List<Integer> sorted) {
    Container inventory = getContainerInventory(player);
    if (inventory == null) {
      return;
    }

    SlotRange slotRange = SlotRange.fullRange(inventory);
    applySort(player, inventory, slotRange, sorted);
  }

  private static void applySort(Player player, Container inventory, SlotRange slotRange, List<Integer> sorted) {
    HashSet<Integer> slotsWithItems = new HashSet<>();
    for (int i : slotRange.getSlots()) {
      if (!inventory.getItem(i).isEmpty()) {
        slotsWithItems.add(i);
      }
    }

    NonNullList<ItemStack> reconstructed = NonNullList.withSize(sorted.size(), ItemStack.EMPTY);
    for (int destIndex = 0; destIndex < sorted.size(); destIndex++) {
      int srcIndex = sorted.get(destIndex);
      ItemStack stack = srcIndex == -1 ? ItemStack.EMPTY : inventory.getItem(srcIndex).copy();
      reconstructed.set(destIndex, stack);

      if (srcIndex > -1 && !slotsWithItems.remove(srcIndex)) {
        return;
      }
    }

    if (!slotsWithItems.isEmpty()) {
      return;
    }

    for (int i = 1; i < reconstructed.size(); i++) {
      ItemStack current = reconstructed.get(i);
      if (current.isEmpty()) {
        continue;
      }

      for (int j = i - 1; j >= 0 && !current.isEmpty(); j--) {
        ItemStack dest = reconstructed.get(j);

        if (dest.isEmpty()) {
          continue;
        }
        if (!canStacksBeMerged(dest, current)) {
          break;
        }

        mergeStacks(dest, current);
        if (current.isEmpty()) {
          reconstructed.set(i, ItemStack.EMPTY);
        }
      }
    }

    Iterator<ItemStack> merged = reconstructed.iterator();
    for (int slotIndex : slotRange.getSlots()) {
      ItemStack stack = ItemStack.EMPTY;
      while (merged.hasNext()) {
        stack = merged.next();
        if (!stack.isEmpty()) {
          break;
        }
      }
      inventory.setItem(slotIndex, stack);
    }
  }

  public static void autoStack(Player player, boolean fromPlayerInventory) {
    Container containerInventory = getContainerInventory(player);
    if (containerInventory == null) {
      return;
    }

    Container playerInventory = player.getInventory();
    SlotRange playerSlotRange = SlotRange.playerMainRange();
    SlotRange containerSlotRange = SlotRange.fullRange(containerInventory);

    if (fromPlayerInventory) {
      autoStackInventories(playerInventory, playerSlotRange, containerInventory, containerSlotRange, player);
    } else {
      autoStackInventories(containerInventory, containerSlotRange, playerInventory, playerSlotRange, player);
    }
  }

  private static void autoStackInventories(
      Container from, SlotRange fromRange, Container to, SlotRange toRange, Player player
  ) {
    transferEntireInventory(from, fromRange, to, toRange, (fromStack, toStack) -> !toStack.isEmpty(), player);
  }

  public static void transferAll(Player player, boolean fromPlayerInventory) {
    Container containerInventory = getContainerInventory(player);
    if (containerInventory == null) {
      return;
    }

    Container playerInventory = player.getInventory();
    SlotRange playerSlotRange = SlotRange.playerMainRange();
    SlotRange containerSlotRange = SlotRange.fullRange(containerInventory);

    if (player.containerMenu instanceof HorseInventoryMenu) {
      containerSlotRange = SlotRange.horseMainRange(containerInventory);
    }

    if (fromPlayerInventory) {
      transferEntireInventory(
          playerInventory,
          playerSlotRange,
          containerInventory,
          containerSlotRange,
          player.inventoryMenu,
          player.containerMenu,
          player
      );
    } else {
      transferEntireInventory(
          containerInventory,
          containerSlotRange,
          playerInventory,
          playerSlotRange,
          player.containerMenu,
          player.inventoryMenu,
          player
      );
    }
  }

  private static void transferEntireInventory(
      Container from,
      SlotRange fromRange,
      Container to,
      SlotRange toRange,
      BiFunction<ItemStack, ItemStack, Boolean> predicate,
      Player player
  ) {
    transferEntireInventory(from, fromRange, to, toRange, predicate, null, null, player);
  }

  private static void transferEntireInventory(
      Container from,
      SlotRange fromRange,
      Container to,
      SlotRange toRange,
      net.minecraft.world.inventory.AbstractContainerMenu fromScreenHandler,
      net.minecraft.world.inventory.AbstractContainerMenu toScreenHandler,
      Player player
  ) {
    transferEntireInventory(
        from,
        fromRange,
        to,
        toRange,
        (fromStack, toStack) -> true,
        fromScreenHandler,
        toScreenHandler,
        player
    );
  }

  private static void transferEntireInventory(
      Container from,
      SlotRange fromRange,
      Container to,
      SlotRange toRange,
      BiFunction<ItemStack, ItemStack, Boolean> predicate,
      net.minecraft.world.inventory.AbstractContainerMenu fromScreenHandler,
      net.minecraft.world.inventory.AbstractContainerMenu toScreenHandler,
      Player player
  ) {
    for (int toIdx : toRange.getSlots()) {
      for (int fromIdx : fromRange.getSlots()) {
        ItemStack fromStack = from.getItem(fromIdx).copy();
        ItemStack toStack = to.getItem(toIdx).copy();

        if (fromStack.isEmpty()) {
          continue;
        }

        if (!predicate.apply(fromStack, toStack)) {
          continue;
        }

        if (!canTakeItemFromSlot(fromScreenHandler, fromIdx, player)) {
          continue;
        }

        if (!canPlaceItemInSlot(toScreenHandler, toIdx, fromStack)) {
          continue;
        }

        if (canStacksBeMerged(toStack, fromStack)) {
          if (mergeStacks(toStack, fromStack)) {
            to.setItem(toIdx, toStack);
            from.setItem(fromIdx, fromStack.isEmpty() ? ItemStack.EMPTY : fromStack);
          }
        } else if (toStack.isEmpty() && !fromStack.isEmpty()) {
          to.setItem(toIdx, fromStack);
          from.setItem(fromIdx, ItemStack.EMPTY);
        }
      }
    }
  }
}
