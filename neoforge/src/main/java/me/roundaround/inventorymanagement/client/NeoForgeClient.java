package me.roundaround.inventorymanagement.client;

import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.InventoryManagementWorldConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeClient {
  public static void init(IEventBus modBus, ModContainer container) {
    modBus.addListener(
        FMLClientSetupEvent.class, _ -> {
          InventoryManagementWorldConfig.getInstance().init();
          InventoryButtonsManager.INSTANCE.init();
          ConfigControlRegister.init();
          InventoryManagementKeyMappings.init();
          HotbarSwapClient.init();
          DurabilityClient.init();

          BuiltinResourcePack.register(
              Constants.MOD_ID,
              "inventorymanagement-dark-ui",
              Component.translatable("inventorymanagement.resourcepack.dark")
          );
        }
    );

    container.registerExtensionPoint(
        IConfigScreenFactory.class,
        (_, parent) -> new me.roundaround.trove.client.gui.screen.ConfigScreen(
            parent,
            Constants.MOD_ID,
            InventoryManagementConfig.getInstance()
        )
    );
  }

  private NeoForgeClient() {
  }
}
