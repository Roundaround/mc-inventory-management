package me.roundaround.inventorymanagement.config;

import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.config.ConfigPath;
import me.roundaround.trove.config.manage.ModConfigImpl;
import me.roundaround.trove.config.manage.store.WorldScopedFileStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Single-player locked player-inventory slots, stored per-save via {@link WorldScopedFileStore} so
 * they land in {@code <world>/config/inventorymanagement.toml} and travel with the save. The lone
 * option is {@code singlePlayerOnly}, so it is inactive — and this store dormant — anywhere but
 * single-player; multiplayer locks live in the global client config's per-server map
 * ({@link ServerLockedSlotsConfigOption}) instead. Routing between the two lives in
 * {@link InventoryManagementConfig#getLockedPlayerSlots()} /
 * {@link InventoryManagementConfig#toggleLockedPlayerSlot(int)}.
 *
 * <p>{@code .init()} only subscribes to the world-dir lifecycle; the option is (re)built and read on
 * each world attach and dropped on detach, so the field is {@code null} until a single-player world
 * is loaded — every accessor here guards for that.
 */
public class InventoryManagementWorldConfig extends ModConfigImpl implements WorldScopedFileStore {
  private static final ConfigPath LOCKED_PATH = ConfigPath.of("lockedPlayerSlots");

  private static InventoryManagementWorldConfig instance;

  /** Null until a single-player world attaches (see class doc); reset to null on world detach. */
  public IntListConfigOption lockedPlayerSlots;

  public InventoryManagementWorldConfig() {
    super(Constants.MOD_ID);
  }

  public static InventoryManagementWorldConfig getInstance() {
    if (instance == null) {
      instance = new InventoryManagementWorldConfig();
    }
    return instance;
  }

  @Override
  protected void registerOptions() {
    this.lockedPlayerSlots = this.buildRegistration(IntListConfigOption.builder(LOCKED_PATH)
        .setComment(
            "Player main-inventory slot indices locked from sorting, auto-stacking, and transfer-all in THIS single-player world. Managed programmatically; no GUI control.")
        .build()).singlePlayerOnly().noGuiControl().commit();
  }

  @Override
  public void clear() {
    super.clear();
    // byPath/byGroup are cleared by super; drop our direct handle too so accessors don't read a
    // stale (previous-world) option between detach and the next attach.
    this.lockedPlayerSlots = null;
  }

  /** The locked slots for the loaded single-player world, or empty when no world is attached. */
  public List<Integer> getLockedPlayerSlots() {
    if (this.lockedPlayerSlots == null) {
      return List.of();
    }
    return this.lockedPlayerSlots.getValue();
  }

  /** Flips {@code slot}'s locked state for the loaded world and persists to the per-world file. */
  public void toggleLockedPlayerSlot(int slot) {
    if (this.lockedPlayerSlots == null) {
      return;
    }
    List<Integer> current = new ArrayList<>(this.lockedPlayerSlots.getPendingValue());
    if (current.contains(slot)) {
      current.remove(Integer.valueOf(slot));
    } else {
      current.add(slot);
    }
    this.lockedPlayerSlots.setValue(current);
    this.writeToStore();
  }
}
