package me.roundaround.inventorymanagement.client;

import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.InventoryManagementWorldConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ForgeClient {
  public static void init(FMLJavaModLoadingContext context) {
    FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(event -> {
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
    });

    context.getContainer().registerExtensionPoint(
        ConfigScreenHandler.ConfigScreenFactory.class,
        () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new me.roundaround.trove.client.gui.screen.ConfigScreen(
            parent,
            Constants.MOD_ID,
            InventoryManagementConfig.getInstance()
        ))
    );
  }

  private ForgeClient() {
  }
}
