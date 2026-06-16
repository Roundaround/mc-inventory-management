package me.roundaround.inventorymanagement;

import me.roundaround.inventorymanagement.api.sorting.DynamicTagGroups;
import me.roundaround.inventorymanagement.api.sorting.GroupBootstrap;
import me.roundaround.inventorymanagement.client.ForgeClient;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.forge.TroveForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("inventorymanagement")
public final class InventoryManagementForgeMod {
  public InventoryManagementForgeMod(FMLJavaModLoadingContext context) {
    TroveForge.bootstrap(context);
    InventoryManagementConfig.getInstance().init();
    GroupBootstrap.init();
    Networking.register();

    TagsUpdatedEvent.BUS.addListener(event -> {
      if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
        DynamicTagGroups.rebuild(event.getRegistryAccess());
      }
    });

    if (FMLEnvironment.dist == Dist.CLIENT) {
      ForgeClient.init(context);
    }
  }
}
