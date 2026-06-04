package me.roundaround.inventorymanagement.client;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.network.chat.Component;

@Entrypoint(Entrypoint.CLIENT)
public class InventoryManagementClientMod implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
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
}
