package me.roundaround.inventorymanagement;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.network.Networking;
import net.fabricmc.api.ModInitializer;

@Entrypoint(Entrypoint.MAIN)
public final class InventoryManagementMod implements ModInitializer {
  @Override
  public void onInitialize() {
    InventoryManagementConfig.getInstance().init();
    Networking.register();
  }
}
