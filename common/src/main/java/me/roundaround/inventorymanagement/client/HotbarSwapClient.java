package me.roundaround.inventorymanagement.client;

import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.event.ClientLifecycle;
import net.minecraft.client.Minecraft;

/**
 * Central client-side state and actions for the Hotbar swapping feature.
 *
 * <p>State is a single in-memory {@code swappedRow} in {@code {0, 1, 2, 3}} (0 = none/normal
 * layout). It is <em>not</em> persisted to disk and is reset to 0 on disconnect (when
 * {@link Minecraft#player} becomes {@code null}). Each transition to a new row sends a swap packet
 * carrying {@code (previousRow, newRow)}; the server restores {@code previousRow} to its home first
 * and then applies {@code newRow}, so only one row is ever out of place at a time.
 *
 * <p>The {@link #reset()} escape hatch clears client tracking back to 0 <em>without</em> sending a
 * packet or moving items — re-baselining to the current physical layout after a desync (e.g. after
 * quitting mid-swap).
 */
public final class HotbarSwapClient {
  /** Number of swappable main-inventory rows (row 1 = slots 9-17, row 3 = slots 27-35). */
  public static final int ROW_COUNT = 3;

  private static int swappedRow = 0;

  private static boolean initialized = false;

  private HotbarSwapClient() {
  }

  private static InventoryManagementConfig config() {
    return InventoryManagementConfig.getInstance();
  }

  /** The currently-swapped row in {@code {0, 1, 2, 3}}; 0 means the normal layout. */
  public static int getSwappedRow() {
    return swappedRow;
  }

  /**
   * Whether the hotbar-swap modifier keybind is being held while the feature is usable: the config
   * is initialized, {@code modEnabled} and the {@code enableHotbarSwap} feature toggle are on, and the
   * local player exists. The scroll path additionally requires no screen to be open; callers needing
   * that may also check {@code Minecraft.getInstance().screen == null}. Gating here inerts both input
   * paths (scroll and number keys) at once, so a disabled feature can never raise {@code swappedRow}.
   */
  public static boolean isModifierActive() {
    if (!config().isInitialized() || !config().modEnabled.getValue() || !config().enableHotbarSwap.getValue()) {
      return false;
    }
    if (Minecraft.getInstance().player == null) {
      return false;
    }
    return KeyBindings.isHeld(InventoryManagementKeyMappings.hotbarSwapModifier);
  }

  /** Whether number keys 1-3 may select a row while the modifier is held (config-gated, default off). */
  public static boolean numberKeysEnabled() {
    // isInitialized() is set true only after registerOptions() assigns every option field, so once it
    // returns true hotbarSwapNumberKeys is non-null — matching the field-deref style of isModifierActive,
    // HotbarSwapHud, and HotbarSwapIndicatorMixin (no redundant null check).
    if (!config().isInitialized()) {
      return false;
    }
    return config().hotbarSwapNumberKeys.getValue();
  }

  /**
   * Cycle the swapped row in response to a scroll. Positive {@code wheel} (scroll up) steps backward,
   * negative steps forward, wrapping through the four states 0 -> 1 -> 2 -> 3 -> 0.
   */
  public static void cycle(double wheel) {
    int dir = wheel > 0 ? -1 : 1;
    goToRow(Math.floorMod(swappedRow + dir, ROW_COUNT + 1));
  }

  /**
   * Select {@code row} directly (1-3), or toggle back to 0 if {@code row} already matches the active
   * swapped row.
   */
  public static void pressRow(int row) {
    goToRow(row == swappedRow ? 0 : row);
  }

  /**
   * Transition to {@code target} (0-3). No-op if already there; otherwise sends a swap packet
   * carrying the previous and new rows so the server restores the previous row before applying the
   * new one.
   */
  public static void goToRow(int target) {
    if (target == swappedRow) {
      return;
    }
    int prev = swappedRow;
    swappedRow = target;
    ClientNetworking.sendHotbarSwap(prev, target);
  }

  /**
   * Clear client tracking back to the normal layout WITHOUT sending a packet or moving items. The
   * escape hatch for desync: re-baselines to whatever is physically in the hotbar right now.
   */
  public static void reset() {
    swappedRow = 0;
  }

  /**
   * Wire up the per-tick handling for the reset keybind and the disconnect re-baseline. Idempotent.
   *
   * <p>The in-game swapped-row badge is drawn by {@code HotbarSwapHudMixin} (a {@code
   * Gui#extractItemHotbar} TAIL inject), not a HUD layer — see {@code HotbarSwapHud}.
   */
  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;

    ClientLifecycle.onTick(() -> {
      while (InventoryManagementKeyMappings.hotbarSwapReset.consumeClick()) {
        reset();
      }
      if (Minecraft.getInstance().player == null && swappedRow != 0) {
        reset();
      }
    });
  }
}
