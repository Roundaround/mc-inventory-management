package me.roundaround.inventorymanagement.neoforge;

import me.roundaround.inventorymanagement.client.InventoryButtonsManager;
import me.roundaround.inventorymanagement.client.gui.screen.PerScreenPositionEditScreen;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.ScreenWidgets;
import me.roundaround.trove.event.ScreenInput;
import me.roundaround.trove.neoforge.TroveNeoForge;
import me.roundaround.trove.resource.BuiltinResourcePack;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;

@Mod("inventorymanagement")
public final class InventoryManagementNeoForgeMod {
    public InventoryManagementNeoForgeMod(IEventBus modBus, ModContainer container) {
        TroveNeoForge.bootstrap(modBus, container);
        InventoryManagementConfig.getInstance().init();
        Networking.register();

        modBus.addListener(FMLClientSetupEvent.class, event -> {
            InventoryButtonsManager.INSTANCE.init();
            ConfigControlRegister.init();
            initKeyBindings();

            BuiltinResourcePack.register(
                    Constants.MOD_ID,
                    "inventorymanagement-dark-ui",
                    Component.translatable("inventorymanagement.resourcepack.dark"));
        });

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new me.roundaround.trove.client.gui.screen.ConfigScreen(
                        parent, Constants.MOD_ID, InventoryManagementConfig.getInstance()));
    }

    private static void initKeyBindings() {
        KeyMapping keybindingPlayer = KeyBindings.register(new KeyMapping(
                "inventorymanagement.keybind.position_edit.player",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyMapping.Category.INVENTORY));
        KeyMapping keybindingContainer = KeyBindings.register(new KeyMapping(
                "inventorymanagement.keybind.position_edit.container",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, KeyMapping.Category.INVENTORY));

        ScreenInput.subscribe((screen, input) -> {
            if (ScreenWidgets.getWidgets(screen).stream().noneMatch(w -> w instanceof InventoryManagementButton)) {
                return false;
            }
            if (keybindingPlayer.matches(input)) {
                GuiUtil.setScreen(new PerScreenPositionEditScreen(screen, true));
                return true;
            }
            if (keybindingContainer.matches(input)) {
                GuiUtil.setScreen(new PerScreenPositionEditScreen(screen, false));
                return true;
            }
            return false;
        });
    }
}
