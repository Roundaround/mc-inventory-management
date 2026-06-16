package me.roundaround.inventorymanagement;

import me.roundaround.inventorymanagement.api.sorting.DynamicTagGroups;
import me.roundaround.inventorymanagement.api.sorting.GroupBootstrap;
import me.roundaround.inventorymanagement.client.NeoForgeClient;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.neoforge.TroveNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod("inventorymanagement")
public final class InventoryManagementNeoForgeMod {
    public InventoryManagementNeoForgeMod(IEventBus modBus, ModContainer container) {
        TroveNeoForge.bootstrap(modBus, container);
        InventoryManagementConfig.getInstance().init();
        GroupBootstrap.init();
        Networking.register();

        NeoForge.EVENT_BUS.addListener(TagsUpdatedEvent.class, event -> {
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
                DynamicTagGroups.rebuild(event.getLookupProvider());
            }
        });

        if (FMLEnvironment.getDist().isClient()) {
            NeoForgeClient.init(modBus, container);
        }
    }
}
