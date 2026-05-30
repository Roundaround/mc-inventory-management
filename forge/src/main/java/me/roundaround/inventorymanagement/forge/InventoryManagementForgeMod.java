package me.roundaround.inventorymanagement.forge;

import me.roundaround.inventorymanagement.api.sorting.DynamicTagGroups;
import me.roundaround.inventorymanagement.api.sorting.GroupBootstrap;
import me.roundaround.inventorymanagement.client.InventoryButtonsManager;
import me.roundaround.inventorymanagement.client.InventoryManagementKeyMappings;
import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.forge.TroveForge;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("inventorymanagement")
public final class InventoryManagementForgeMod {
    public InventoryManagementForgeMod(FMLJavaModLoadingContext context) {
        TroveForge.bootstrap(context);
        InventoryManagementConfig.getInstance().init();
        GroupBootstrap.init();
        Networking.register();

        // Rebuild data-driven grouping families on the eventbus-7 typed game bus when the client
        // receives synced tags (join + /reload). Uses TagsUpdatedEvent.BUS (NOT legacy
        // @SubscribeEvent / MinecraftForge.EVENT_BUS, which would not compile against this build),
        // consistent with the FMLClientSetupEvent.getBus(...).addListener pattern below. The
        // CLIENT_PACKET_RECEIVED guard skips the integrated/dedicated server's SERVER_DATA_LOAD.
        TagsUpdatedEvent.BUS.addListener(event -> {
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
                DynamicTagGroups.rebuild(event.getRegistryAccess());
            }
        });

        FMLClientSetupEvent.getBus(context.getModBusGroup())
                .addListener(event -> {
                    InventoryButtonsManager.INSTANCE.init();
                    ConfigControlRegister.init();
                    InventoryManagementKeyMappings.init();

                    BuiltinResourcePack.register(
                            Constants.MOD_ID,
                            "inventorymanagement-dark-ui",
                            Component.translatable("inventorymanagement.resourcepack.dark"));
                });

        context.getContainer().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> new me.roundaround.trove.client.gui.screen.ConfigScreen(
                                parent, Constants.MOD_ID, InventoryManagementConfig.getInstance())));
    }
}
