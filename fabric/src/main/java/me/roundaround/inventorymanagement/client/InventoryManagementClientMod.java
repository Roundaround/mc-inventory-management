package me.roundaround.inventorymanagement.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.allay.api.Entrypoint;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.screen.PerScreenPositionEditScreen;
import me.roundaround.inventorymanagement.compat.trove.ConfigControlRegister;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.ScreenWidgets;
import me.roundaround.trove.event.ScreenInput;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Entrypoint(Entrypoint.CLIENT)
public class InventoryManagementClientMod implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    InventoryButtonsManager.INSTANCE.init();
    ConfigControlRegister.init();
    initKeyBindings();

    BuiltinResourcePack.register(
        Constants.MOD_ID,
        "inventorymanagement-dark-ui",
        Component.translatable("inventorymanagement.resourcepack.dark")
    );
  }

  private static void initKeyBindings() {
    KeyMapping keybindingPlayer = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.position_edit.player",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        KeyMapping.Category.INVENTORY
    ));

    KeyMapping keybindingContainer = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.position_edit.container",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_L,
        KeyMapping.Category.INVENTORY
    ));

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
