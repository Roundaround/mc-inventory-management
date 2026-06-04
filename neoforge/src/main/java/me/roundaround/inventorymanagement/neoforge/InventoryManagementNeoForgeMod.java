package me.roundaround.inventorymanagement.neoforge;

import me.roundaround.inventorymanagement.api.sorting.DynamicTagGroups;
import me.roundaround.inventorymanagement.api.sorting.GroupBootstrap;
import me.roundaround.inventorymanagement.client.DurabilityClient;
import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import me.roundaround.inventorymanagement.client.InventoryButtonsManager;
import me.roundaround.inventorymanagement.client.InventoryManagementKeyMappings;
import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.neoforge.TroveNeoForge;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod("inventorymanagement")
public final class InventoryManagementNeoForgeMod {
    public InventoryManagementNeoForgeMod(IEventBus modBus, ModContainer container) {
        TroveNeoForge.bootstrap(modBus, container);
        InventoryManagementConfig.getInstance().init();
        GroupBootstrap.init();
        Networking.register();

        // Rebuild data-driven grouping families on the GAME bus when the client receives synced tags
        // (join + /reload). MUST be the game bus (NeoForge.EVENT_BUS), not the @Mod-constructor
        // modBus — a modBus registration would silently never fire. The CLIENT_PACKET_RECEIVED guard
        // skips the integrated/dedicated server's SERVER_DATA_LOAD; sorting is client-side.
        NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.class, event -> {
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
                DynamicTagGroups.rebuild(event.getLookupProvider());
            }
        });

        modBus.addListener(FMLClientSetupEvent.class, event -> {
            InventoryButtonsManager.INSTANCE.init();
            ConfigControlRegister.init();
            InventoryManagementKeyMappings.init();
            HotbarSwapClient.init();
            DurabilityClient.init();

            BuiltinResourcePack.register(
                    Constants.MOD_ID,
                    "inventorymanagement-dark-ui",
                    Component.translatable("inventorymanagement.resourcepack.dark"));
        });

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new me.roundaround.trove.client.gui.screen.ConfigScreen(
                        parent, Constants.MOD_ID, InventoryManagementConfig.getInstance()));
    }
}
