package me.roundaround.inventorymanagement.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.screen.PerScreenPositionEditScreen;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.ScreenWidgets;
import me.roundaround.trove.event.ScreenInput;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * All of the mod's client keybinds, registered in loader-neutral common code so the per-loader
 * entrypoints just call {@link #init()} instead of each duplicating the registration and the
 * position-edit input handler.
 *
 * <ul>
 *   <li>{@link #positionEditPlayer} / {@link #positionEditContainer} open the per-screen button
 *       position editor; handled here via a single {@link ScreenInput} subscription.</li>
 *   <li>{@link #peekLockedSlots} is read by {@code SlotLockMixin} to reveal the locked-slot marker in
 *       {@link me.roundaround.inventorymanagement.config.LockedSlotDisplay#HOTKEY} mode. Defaults to
 *       unbound; held-state is polled via Trove's screen-safe {@link KeyBindings#isHeld}.</li>
 * </ul>
 */
public final class InventoryManagementKeyBindings {
  public static KeyMapping positionEditPlayer;
  public static KeyMapping positionEditContainer;
  public static KeyMapping peekLockedSlots;

  private static boolean initialized = false;

  private InventoryManagementKeyBindings() {
  }

  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;

    positionEditPlayer = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.position_edit.player",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        KeyMapping.Category.INVENTORY
    ));

    positionEditContainer = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.position_edit.container",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_L,
        KeyMapping.Category.INVENTORY
    ));

    peekLockedSlots = KeyBindings.register(new KeyMapping(
        "inventorymanagement.keybind.peek_locked_slots",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        KeyMapping.Category.INVENTORY
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
