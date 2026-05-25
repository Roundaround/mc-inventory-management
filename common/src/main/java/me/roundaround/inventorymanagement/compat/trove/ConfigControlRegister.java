package me.roundaround.inventorymanagement.compat.trove;

import me.roundaround.inventorymanagement.client.gui.screen.DefaultPositionEditScreen;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.trove.client.gui.widget.config.ControlRegistry;
import me.roundaround.trove.client.gui.widget.config.SubScreenControl;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.Minecraft;

public final class ConfigControlRegister {
  public static void init() {
    try {
      ControlRegistry.register(
          InventoryManagementConfig.getInstance().defaultPosition.getId(),
          ConfigControlRegister::getSubScreenControl);
    } catch (ControlRegistry.RegistrationException e) {
      // Deal with this later xD
    }
  }

  private static SubScreenControl<Position, PositionConfigOption> getSubScreenControl(
      Minecraft client,
      PositionConfigOption option,
      int width,
      int height) {
    return new SubScreenControl<>(
        client,
        option,
        width,
        height,
        SubScreenControl.getValueDisplayMessageFactory(),
        DefaultPositionEditScreen.getSubScreenFactory());
  }

  private ConfigControlRegister() {
  }
}
