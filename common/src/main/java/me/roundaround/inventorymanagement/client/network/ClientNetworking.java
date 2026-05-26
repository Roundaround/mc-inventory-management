package me.roundaround.inventorymanagement.client.network;

import me.roundaround.inventorymanagement.client.inventory.ClientInventoryHelper;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class ClientNetworking {
  private ClientNetworking() {
  }

  public static void sendStack(boolean fromPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.StackC2S(fromPlayerInventory));
  }

  public static void sendSort(Player player, boolean isPlayerInventory) {
    List<Integer> sorted = isPlayerInventory
        ? ClientInventoryHelper.calculatePlayerSort(player)
        : ClientInventoryHelper.calculateContainerSort(player);
    TroveNetworking.sendToServer(new Networking.SortC2S(isPlayerInventory, sorted));
  }

  public static void sendSortAll(Player player) {
    List<Integer> playerSorted = ClientInventoryHelper.calculatePlayerSort(player);
    List<Integer> containerSorted = ClientInventoryHelper.calculateContainerSort(player);
    if (containerSorted.isEmpty()) {
      TroveNetworking.sendToServer(new Networking.SortC2S(true, playerSorted));
    } else {
      TroveNetworking.sendToServer(new Networking.SortAllC2S(playerSorted, containerSorted));
    }
  }

  public static void sendTransfer(boolean fromPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.TransferC2S(fromPlayerInventory));
  }
}
