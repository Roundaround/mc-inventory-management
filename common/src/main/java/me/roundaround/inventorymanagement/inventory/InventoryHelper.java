package me.roundaround.inventorymanagement.inventory;

import me.roundaround.inventorymanagement.inventory.sorting.ItemStackComparator;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class InventoryHelper {
  public static void sortInventory(Player player, boolean isPlayerInventory) {
    Container containerInventory = getContainerInventory(player);
    Container inventory = isPlayerInventory || containerInventory == null ? player.getInventory() : containerInventory;

    if (inventory instanceof Inventory) {
      sortInventory(inventory, SlotRange.playerMainRange());
    } else {
      sortInventory(inventory);
    }
  }

  private static void sortInventory(Container inventory) {
    sortInventory(inventory, 0, inventory.getContainerSize());
  }

  private static void sortInventory(Container inventory, int start, int end) {
    sortInventory(inventory, new SlotRange(start, end));
  }

  private static void sortInventory(Container inventory, SlotRange slotRange) {
    List<ItemStack> stacks = new ArrayList<>();

    for (int i = slotRange.min; i < slotRange.max; i++) {
      stacks.add(inventory.getItem(i).copy());
    }

    List<ItemStack> cleanedStacks = stacks.stream()
        .filter(itemStack -> !itemStack.isEmpty())
        .map(ItemStack::copy)
        .toList();

    for (int i = 0; i < cleanedStacks.size(); i++) {
      for (int j = i + 1; j < cleanedStacks.size(); j++) {
        ItemStack a = cleanedStacks.get(i);
        ItemStack b = cleanedStacks.get(j);

        if (areItemStacksMergeable(a, b)) {
          int itemsToShift = Math.min(a.getMaxStackSize() - a.getCount(), b.getCount());
          if (itemsToShift > 0) {
            a.grow(itemsToShift);
            b.shrink(itemsToShift);
          }
        }
      }
    }

    List<ItemStack> sortedStacks = cleanedStacks.stream()
        .filter(itemStack -> !itemStack.isEmpty())
        .sorted(ItemStackComparator.comparator())
        .toList();

    for (int i = slotRange.min; i < slotRange.max; i++) {
      int j = i - slotRange.min;
      ItemStack itemStack = j >= sortedStacks.size() ? ItemStack.EMPTY : sortedStacks.get(j);
      inventory.setItem(i, itemStack);
    }
  }

  public static void autoStack(Player player, boolean fromPlayerInventory) {
    Container containerInventory = getContainerInventory(player);
    if (containerInventory == null) {
      return;
    }

    Container playerInventory = player.getInventory();

    if (fromPlayerInventory) {
      autoStackInventories(playerInventory, containerInventory, player);
    } else {
      autoStackInventories(containerInventory, playerInventory, player);
    }
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
          containerInventory,
          playerSlotRange,
          containerSlotRange,
          player.inventoryMenu,
          player.containerMenu,
          player
      );
    } else {
      transferEntireInventory(
          containerInventory,
          playerInventory,
          containerSlotRange,
          playerSlotRange,
          player.containerMenu,
          player.inventoryMenu,
          player
      );
    }
  }

  private static void autoStackInventories(Container from, Container to, Player player) {
    autoStackInventories(from, to, SlotRange.fullRange(from), SlotRange.fullRange(to), player);
  }

  private static void autoStackInventories(
      Container from,
      Container to,
      SlotRange fromRange,
      SlotRange toRange,
      Player player
  ) {
    transferEntireInventory(from, to, fromRange, toRange, (fromStack, toStack) -> !toStack.isEmpty(), player);
  }

  private static void transferEntireInventory(
      Container from,
      Container to,
      SlotRange fromRange,
      SlotRange toRange,
      BiFunction<ItemStack, ItemStack, Boolean> predicate,
      Player player
  ) {
    transferEntireInventory(from, to, fromRange, toRange, predicate, null, null, player);
  }

  private static void transferEntireInventory(
      Container from,
      Container to,
      SlotRange fromRange,
      SlotRange toRange,
      AbstractContainerMenu fromScreenHandler,
      AbstractContainerMenu toScreenHandler,
      Player player
  ) {
    transferEntireInventory(
        from,
        to,
        fromRange,
        toRange,
        (fromStack, toStack) -> true,
        fromScreenHandler,
        toScreenHandler,
        player
    );
  }

  private static void transferEntireInventory(
      Container from,
      Container to,
      SlotRange fromRange,
      SlotRange toRange,
      BiFunction<ItemStack, ItemStack, Boolean> predicate,
      AbstractContainerMenu fromScreenHandler,
      AbstractContainerMenu toScreenHandler,
      Player player
  ) {
    for (int toIdx = toRange.min; toIdx < toRange.max; toIdx++) {
      for (int fromIdx = fromRange.min; fromIdx < fromRange.max; fromIdx++) {
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

        if (areItemStacksMergeable(toStack, fromStack)) {
          int space = toStack.getMaxStackSize() - toStack.getCount();
          int amount = Math.min(space, fromStack.getCount());
          if (amount > 0) {
            toStack.grow(amount);
            fromStack.shrink(amount);

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

  private static boolean canTakeItemFromSlot(AbstractContainerMenu screenHandler, int idx, Player player) {
    if (screenHandler == null) {
      return true;
    }
    try {
      return screenHandler.getSlot(idx).mayPickup(player);
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }

  private static boolean canPlaceItemInSlot(AbstractContainerMenu screenHandler, int idx, ItemStack itemStack) {
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

  public static boolean areItemStacksMergeable(ItemStack a, ItemStack b) {
    return !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
  }

  static class SlotRange {
    public int min;
    public int max;

    public SlotRange(int min, int max) {
      this.min = min;
      this.max = max;
    }

    public static SlotRange fullRange(Container inventory) {
      return new SlotRange(0, inventory.getContainerSize());
    }

    public static SlotRange playerMainRange() {
      return new SlotRange(Inventory.getSelectionSize(), Inventory.INVENTORY_SIZE);
    }

    public static SlotRange horseMainRange(Container inventory) {
      return new SlotRange(2, inventory.getContainerSize());
    }
  }
}
