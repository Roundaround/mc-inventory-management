package me.roundaround.inventorymanagement;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.inventorymanagement.api.sorting.DynamicTagGroups;
import me.roundaround.inventorymanagement.api.sorting.GroupBootstrap;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.network.Networking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;

@Entrypoint(Entrypoint.MAIN)
public final class InventoryManagementMod implements ModInitializer {
  @Override
  public void onInitialize() {
    InventoryManagementConfig.getInstance().init();
    GroupBootstrap.init();
    Networking.register();

    // Rebuild data-driven grouping families when the client receives synced tags (join + /reload).
    // The server-side data-load fire has client == false and is skipped; sorting is client-side.
    CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
      if (client) {
        DynamicTagGroups.rebuild(registries);
      }
    });
  }
}
