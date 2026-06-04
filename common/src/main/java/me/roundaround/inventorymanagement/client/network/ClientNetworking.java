package me.roundaround.inventorymanagement.client.network;

import me.roundaround.inventorymanagement.client.inventory.ClientInventoryHelper;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.inventory.IgnoredSlots;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class ClientNetworking {
  private ClientNetworking() {
  }

  private static long lockedMask() {
    return IgnoredSlots.maskOf(InventoryManagementConfig.getInstance().getLockedPlayerSlots());
  }

  public static void sendStack(boolean fromPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.StackC2S(fromPlayerInventory, lockedMask()));
  }

  public static void sendSort(Player player, boolean isPlayerInventory) {
    long locked = lockedMask();
    List<Integer> sorted = isPlayerInventory
        ? ClientInventoryHelper.calculatePlayerSort(player, locked)
        : ClientInventoryHelper.calculateContainerSort(player);
    TroveNetworking.sendToServer(new Networking.SortC2S(isPlayerInventory, sorted, locked));
  }

  public static void sendSortAll(Player player) {
    long locked = lockedMask();
    List<Integer> playerSorted = ClientInventoryHelper.calculatePlayerSort(player, locked);
    List<Integer> containerSorted = ClientInventoryHelper.calculateContainerSort(player);
    if (containerSorted.isEmpty()) {
      TroveNetworking.sendToServer(new Networking.SortC2S(true, playerSorted, locked));
    } else {
      TroveNetworking.sendToServer(new Networking.SortAllC2S(playerSorted, containerSorted, locked));
    }
  }

  public static void sendTransfer(boolean fromPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.TransferC2S(fromPlayerInventory, lockedMask()));
  }

  public static void sendHotbarSwap(int previousRow, int newRow) {
    TroveNetworking.sendToServer(new Networking.HotbarSwapC2S(previousRow, newRow));
  }

  /**
   * Requests the server swap the replacement at inventory {@code fromSlot} into {@code targetSlot} for the
   * client-driven auto-replace feature. The client has already decided (per its own config) that the item
   * in {@code targetSlot} is about to break and that this is a valid replacement; the server re-validates.
   */
  public static void sendDurabilityReplace(int fromSlot, EquipmentSlot targetSlot, boolean similar) {
    TroveNetworking.sendToServer(new Networking.DurabilityReplaceC2S(fromSlot, targetSlot, similar));
  }
}
