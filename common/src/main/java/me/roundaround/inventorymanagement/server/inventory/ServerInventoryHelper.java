package me.roundaround.inventorymanagement.server.inventory;

import me.roundaround.inventorymanagement.inventory.IgnoredSlots;
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

  /**
   * Apply a hotbar row swap requested by the client. {@code previousRow} (the row that was swapped in
   * before this request) is restored to its home first, then {@code newRow} is swapped into the
   * hotbar, so at most one row is ever displaced at a time. Both are clamped to {@code 0..3}; 0 means
   * "no row" (normal layout). No-op when they are equal.
   *
   * <p>The whole-row swap intentionally ignores the locked-slots feature: it exchanges all nine slots
   * of a row with the nine hotbar slots regardless of any locks, since the swap is a transient display
   * convenience rather than a sort/transfer operation.
   */
  public static void swapHotbarRows(Player player, int previousRow, int newRow) {
    previousRow = Math.max(0, Math.min(3, previousRow));
    newRow = Math.max(0, Math.min(3, newRow));
    if (previousRow == newRow) {
      return;
    }

    Container inv = player.getInventory();
    // A real player inventory is always >= 36 (36 main slots + armor/offhand, getContainerSize() ~41),
    // so this never trips today. Kept as a guard in case this is ever called with a smaller Container,
    // since the slot math below assumes the vanilla hotbar (0-8) + main-grid (9-35) layout.
    if (inv.getContainerSize() < 36) {
      return;
    }

    if (previousRow != 0) {
      swapRowWithHotbar(inv, previousRow);
    }
    if (newRow != 0) {
      swapRowWithHotbar(inv, newRow);
    }
  }

  private static void swapRowWithHotbar(Container inv, int row) {
    int base = 9 + (row - 1) * 9;
    for (int i = 0; i < 9; i++) {
      ItemStack hot = inv.getItem(i).copy();
      ItemStack rowStack = inv.getItem(base + i).copy();
      inv.setItem(i, rowStack);
      inv.setItem(base + i, hot);
    }
  }

  public static void applySort(Player player, boolean isPlayerInventory, List<Integer> sorted, long lockedMask) {
    if (isPlayerInventory) {
      applyPlayerSort(player, sorted, lockedMask);
    } else {
      applyContainerSort(player, sorted);
    }
  }

  public static void applyPlayerSort(Player player, List<Integer> sorted, long lockedMask) {
    Container inventory = player.getInventory();
    SlotRange slotRange = IgnoredSlots.playerLockedRange(lockedMask);
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

  public static void autoStack(Player player, boolean fromPlayerInventory, long lockedMask) {
    Container containerInventory = getContainerInventory(player);
    if (containerInventory == null) {
      return;
    }

    Container playerInventory = player.getInventory();
    SlotRange containerSlotRange = SlotRange.fullRange(containerInventory);

    if (fromPlayerInventory) {
      // Player inventory is the source: locked slots must not give up their items.
      SlotRange playerSlotRange = IgnoredSlots.playerLockedRange(lockedMask);
      autoStackInventories(playerInventory, playerSlotRange, containerInventory, containerSlotRange, player);
    } else {
      // Player inventory is the destination: locked slots may still receive items from the container.
      autoStackInventories(containerInventory, containerSlotRange, playerInventory, SlotRange.playerMainRange(), player);
    }
  }

  private static void autoStackInventories(
      Container from, SlotRange fromRange, Container to, SlotRange toRange, Player player
  ) {
    transferEntireInventory(from, fromRange, to, toRange, (fromStack, toStack) -> !toStack.isEmpty(), player);
  }

  public static void transferAll(Player player, boolean fromPlayerInventory, long lockedMask) {
    Container containerInventory = getContainerInventory(player);
    if (containerInventory == null) {
      return;
    }

    Container playerInventory = player.getInventory();
    SlotRange containerSlotRange = SlotRange.fullRange(containerInventory);

    if (player.containerMenu instanceof HorseInventoryMenu) {
      containerSlotRange = SlotRange.horseMainRange(containerInventory);
    }

    if (fromPlayerInventory) {
      // Player inventory is the source: locked slots must not give up their items.
      transferEntireInventory(
          playerInventory,
          IgnoredSlots.playerLockedRange(lockedMask),
          containerInventory,
          containerSlotRange,
          player.inventoryMenu,
          player.containerMenu,
          player
      );
    } else {
      // Player inventory is the destination: locked slots may still receive items from the container.
      transferEntireInventory(
          containerInventory,
          containerSlotRange,
          playerInventory,
          SlotRange.playerMainRange(),
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
