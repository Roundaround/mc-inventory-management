package me.roundaround.inventorymanagement.compat.trove;

import com.mojang.logging.LogUtils;
import me.roundaround.inventorymanagement.client.gui.screen.DefaultPositionEditScreen;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.SortMode;
import me.roundaround.trove.client.gui.widget.config.ControlRegistry;
import me.roundaround.trove.client.gui.widget.config.SubScreenControl;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public final class ConfigControlRegister {
  private static final Logger LOGGER = LogUtils.getLogger();

  public static void init() {
    try {
      // SortMode is a mod-defined EnumValue, so Trove has no built-in control for it; register the
      // standard enum cycle control or the config GUI throws NotRegisteredException when it builds.
      ControlRegistry.registerOptionList(SortMode.class);

      ControlRegistry.register(
          InventoryManagementConfig.getInstance().defaultPosition.getId(),
          ConfigControlRegister::getSubScreenControl);
    } catch (ControlRegistry.RegistrationException e) {
      // A RegistrationException means a control was registered twice -- a programming error, not a
      // user-facing condition. Surface it loudly instead of swallowing it; the only consequence is a
      // missing/misrendered control on the config screen, so we log rather than crash the game.
      LOGGER.error("Failed to register Inventory Management config controls; the config screen may be "
          + "missing some options", e);
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
