package me.roundaround.inventorymanagement.client.network;

import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.network.TroveNetworking;

public final class ClientNetworking {
  private ClientNetworking() {
  }

  public static void sendStack(boolean fromPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.StackC2S(fromPlayerInventory));
  }

  public static void sendSort(boolean isPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.SortC2S(isPlayerInventory));
  }

  public static void sendTransfer(boolean fromPlayerInventory) {
    TroveNetworking.sendToServer(new Networking.TransferC2S(fromPlayerInventory));
  }
}
