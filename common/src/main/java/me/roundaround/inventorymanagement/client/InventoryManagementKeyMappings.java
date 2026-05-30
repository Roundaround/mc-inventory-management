package me.roundaround.inventorymanagement.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.screen.PerScreenPositionEditScreen;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.ScreenWidgets;
import me.roundaround.trove.event.ScreenInput;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class InventoryManagementKeyMappings {
  public static KeyMapping positionEditPlayer;
  public static KeyMapping positionEditContainer;
  public static KeyMapping peekLockedSlots;

  private static boolean initialized = false;

  private InventoryManagementKeyMappings() {
  }

  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;

    KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(
        Constants.MOD_ID,
        Constants.MOD_ID
    ));

    positionEditPlayer = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.position_edit.player",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        category
    ));

    positionEditContainer = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.position_edit.container",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        category
    ));

    peekLockedSlots = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.peek_locked_slots",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        category
    ));

    ScreenInput.subscribe((screen, input) -> {
      if (ScreenWidgets.getWidgets(screen).stream().noneMatch(w -> w instanceof InventoryManagementButton)) {
        return false;
      }

      if (positionEditPlayer.matches(input)) {
        GuiUtil.setScreen(new PerScreenPositionEditScreen(screen, true));
        return true;
      }
      if (positionEditContainer.matches(input)) {
        GuiUtil.setScreen(new PerScreenPositionEditScreen(screen, false));
        return true;
      }

      return false;
    });
  }
}
