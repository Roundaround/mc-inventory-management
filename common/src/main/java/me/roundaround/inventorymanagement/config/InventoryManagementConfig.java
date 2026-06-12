package me.roundaround.inventorymanagement.config;

import me.roundaround.inventorymanagement.api.sorting.GroupDefs;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.config.ConfigPath;
import me.roundaround.trove.config.manage.ModConfigImpl;
import me.roundaround.trove.config.manage.store.GameScopedFileStore;
import me.roundaround.trove.config.manage.store.WorldScopedFileStore;
import me.roundaround.trove.config.option.BooleanConfigOption;
import me.roundaround.trove.config.option.EnumConfigOption;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.option.StringListConfigOption;
import me.roundaround.trove.config.value.Position;
import me.roundaround.trove.util.PathAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BooleanSupplier;

public class InventoryManagementConfig extends ModConfigImpl implements GameScopedFileStore {
  private static InventoryManagementConfig instance;

  /** Master on/off switch for the whole mod. When off, no buttons are shown and no behavior runs. */
  public BooleanConfigOption modEnabled;

  /** Whether the sort buttons are shown in inventory/container screens. */
  public BooleanConfigOption showSort;

  /** Whether the transfer-all (place/take) buttons are shown. */
  public BooleanConfigOption showTransfer;

  /** Whether the auto-stack buttons are shown. */
  public BooleanConfigOption showStack;

  /**
   * Primary sort ordering: {@link SortMode#ALPHABETICAL} sorts by item name,
   * {@link SortMode#CREATIVE} uses creative-menu order. Rendered in the config GUI as an enum cycle
   * control (registered in {@code ConfigControlRegister}).
   */
  public EnumConfigOption<SortMode> sortMode;

  /**
   * Default button-position offset applied to every screen. Overridden per-screen by
   * {@link #screenPositions}. Disabled in the GUI when no button categories are shown.
   */
  public PositionConfigOption defaultPosition;

  /**
   * Whether content-holding items (shulker boxes, bundles, etc.) sort before other items. Only takes
   * effect in {@link SortMode#ALPHABETICAL} mode (the comparator gates it on alphabetical sorting).
   */
  public BooleanConfigOption containersFirst;

  /**
   * Master toggle for variant grouping. When on, related families (e.g. all wool colors) cluster
   * together during sort, subject to the per-family {@link #groupToggles} and the data-driven
   * {@link #dynamicGroupsEnabled} gate; when off, every item sorts purely by its own name.
   */
  public BooleanConfigOption itemGrouping;

  /**
   * Per-screen button-position overrides keyed by screen, layered over {@link #defaultPosition}. No
   * GUI control; edited in-game via the per-screen position editor and persisted here.
   */
  public PerScreenPositionConfigOption screenPositions;

  /**
   * One default-true {@link BooleanConfigOption} per variant family, keyed by
   * {@link GroupDefs.GroupDef#id()}. Rebuilt by {@link #registerOptions()} on init and on every
   * {@link #syncWithStore()}; keep it a {@link LinkedHashMap} so the GUI renders the toggles in
   * {@link GroupDefs#ALL} order under the single {@code grouping} section.
   */
  public final LinkedHashMap<String, BooleanConfigOption> groupToggles = new LinkedHashMap<>();

  /**
   * Master gate for ALL data-driven (datapack {@code grouping/}-tag) families at once. When off, no
   * dynamic grouping happens regardless of {@link #disabledDynamicGroups}.
   */
  public BooleanConfigOption dynamicGroupsEnabled;

  /**
   * Per-family opt-out for dynamic groups, keyed by each group's full tag-id string (namespaced,
   * e.g. {@code mymod:grouping/gems}). Persisted so the choice survives a {@link #syncWithStore()}
   * rebuild and degrades gracefully when a pack is removed (a stale id is simply inert). No GUI
   * control in v1.
   */
  public StringListConfigOption disabledDynamicGroups;

  /**
   * Per-multiplayer-server locked player main-inventory slots, keyed by server address. Single-player
   * locks live per-save in {@link InventoryManagementWorldConfig} instead; {@link #getLockedPlayerSlots()}
   * and {@link #toggleLockedPlayerSlot(int)} route between the two by connected-world context. Locked
   * slots are excluded from sort, auto-stack, and transfer-all (both directions). Client-only and
   * stateless on the server: the list rides along inside each operation packet. Managed
   * programmatically; no GUI control.
   */
  public ServerLockedSlotsConfigOption serverLockedPlayerSlots;

  /**
   * Master toggle for the Locked Slots feature. When off, Ctrl+click no longer locks/unlocks slots, no
   * lock markers or tooltips are drawn, and previously-locked slots are no longer excluded from sort,
   * auto-stack, or transfer-all. Stored locks are kept (not cleared), so re-enabling restores them.
   * Gated centrally via {@link #isSlotLockingEnabled()}. Client-only.
   */
  public BooleanConfigOption enableSlotLocking;

  /**
   * When the locked-slot marker (darkened background + border under the item) is drawn. Defaults to
   * {@link LockedSlotDisplay#SHOWN} — always visible; {@link LockedSlotDisplay#HOTKEY} restricts it to
   * while the "Peek locked slots" keybind is held. Rendered in the config GUI as an enum cycle control
   * (registered in {@code ConfigControlRegister}), and disabled there while {@link #enableSlotLocking}
   * is off. Does not affect locking itself or the hover tooltip; only the marker rendering in
   * {@code SlotLockMixin}.
   */
  public EnumConfigOption<LockedSlotDisplay> lockedSlotDisplay;

  /**
   * Master toggle for the Hotbar swapping feature. When off, the modifier keybind does nothing — both
   * the scroll and number-key row-selection paths are inert (gated in {@code HotbarSwapClient}) — and
   * the swapped-row badge is hidden. Client-only.
   */
  public BooleanConfigOption enableHotbarSwap;

  /**
   * Whether number keys 1-3 select a row to swap into the hotbar while the hotbar-swap modifier
   * keybind is held. Defaults off; scrolling with the modifier held always works regardless. When on,
   * keys 4-9 are consumed (no hotbar slot change) while the modifier is held. Subordinate to
   * {@link #enableHotbarSwap}, which disables it in the GUI when the feature is off. Client-only.
   */
  public BooleanConfigOption hotbarSwapNumberKeys;

  // ----- Item Durability group ("durability") -----

  /** Master toggle for the client-side low-durability alert (action bar message + anvil ping). */
  public BooleanConfigOption durabilityAlertEnabled;

  /**
   * Percent-of-max durability thresholds (1-99) that trigger a low-durability alert, fired once each
   * as durability crosses them downward. File-edited; no GUI control.
   */
  public IntListConfigOption durabilityAlertThresholds;

  /** Whether to also fire a low-durability alert at exactly 1 durability point remaining. */
  public BooleanConfigOption durabilityAlertAtOne;

  /** Whether the low-durability alert plays the anvil "ping" sound (the action-bar message always shows). */
  public BooleanConfigOption durabilityAlertSound;

  /**
   * Auto-replace a held/worn item right before it breaks. Client-driven: the client detects the imminent
   * break and requests the swap; the server validates and applies it (so the mod must be on the server in
   * multiplayer). No preference is synced. Default off.
   */
  public BooleanConfigOption durabilityAutoReplace;

  /**
   * Relax auto-replace matching from strict (same item + enchantments) to similar (same category,
   * ignoring material/enchantments). Default off.
   */
  public BooleanConfigOption durabilityAutoReplaceSimilar;

  public InventoryManagementConfig() {
    super(Constants.MOD_ID);
  }

  public static InventoryManagementConfig getInstance() {
    if (instance == null) {
      instance = new InventoryManagementConfig();
    }
    return instance;
  }

  @Override
  protected void registerOptions() {
    // Global on/off switch, shown ungrouped at the top of the config screen.
    this.modEnabled = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("modEnabled"))
        .setDefaultValue(true)
        .setComment("Simple toggle for the mod! Set to false to disable.")
        .build()).clientOnly().commit();

    // ----- Hotbar Swapping group ("hotbarSwap") -----

    this.enableHotbarSwap = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "hotbarSwap", "enableHotbarSwap"))
        .setDefaultValue(true)
        .setComment(
            "Master toggle for hotbar swapping. When false, the hotbar-swap modifier keybind does nothing (scroll and number-key row selection are both inert) and the swapped-row badge is hidden.")
        .build()).clientOnly().commit();

    this.hotbarSwapNumberKeys = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "hotbarSwap", "hotbarSwapNumberKeys"))
        .setDefaultValue(true)
        .setComment(
            "While holding the hotbar-swap key, let number keys 1-3 select a row to swap into the hotbar.")
        .onUpdate((option) -> option.setDisabled(!this.enableHotbarSwap.getValue()))
        .build()).clientOnly().commit();

    // ----- Item Durability group -----

    this.durabilityAlertEnabled = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "durability", "durabilityAlertEnabled"))
        .setDefaultValue(true)
        .setComment(
            "Show a low-durability action-bar alert (and anvil ping) when a tool or equipped item drops past a threshold.")
        .build()).clientOnly().commit();

    this.durabilityAlertThresholds = this.buildRegistration(IntListConfigOption.builder(ConfigPath.of(
        "durability", "durabilityAlertThresholds"))
        .setDefaultValue(List.of(10, 5))
        .setComment(
            "Percent-of-max durability thresholds (1-99) that trigger a low-durability alert; each fires once as durability crosses it downward. File-edited; no GUI control.")
        .build()).clientOnly().noGuiControl().commit();

    this.durabilityAlertAtOne = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "durability", "durabilityAlertAtOne"))
        .setDefaultValue(true)
        .setComment("Also fire a low-durability alert at exactly 1 durability point remaining.")
        .build()).clientOnly().commit();

    this.durabilityAlertSound = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "durability", "durabilityAlertSound"))
        .setDefaultValue(true)
        .setComment("Play the anvil ping sound with the low-durability alert (the action-bar message always shows).")
        .build()).clientOnly().commit();

    this.durabilityAutoReplace = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "durability", "durabilityAutoReplace"))
        .setDefaultValue(false)
        .setComment(
            "Right before a held/worn item breaks, swap in a matching replacement from your inventory. Requires the mod on the server in multiplayer.")
        .build()).clientOnly().commit();

    this.durabilityAutoReplaceSimilar = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
        "durability", "durabilityAutoReplaceSimilar"))
        .setDefaultValue(false)
        .setComment(
            "Relax auto-replace matching from strict (same item + enchantments) to similar (same category, ignoring material/enchantments).")
        .build()).clientOnly().commit();

    // ----- Sorting/Transfering group ("sorting") -----

    this.showSort = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("sorting", "showSort"))
        .setDefaultValue(true)
        .setComment("Whether to show sort buttons in the UI.")
        .build()).clientOnly().commit();

    this.showTransfer = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("sorting", "showTransfer"))
        .setDefaultValue(true)
        .setComment("Whether to show transfer buttons in the UI.")
        .build()).clientOnly().commit();

    this.showStack = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("sorting", "showStack"))
        .setDefaultValue(true)
        .setComment("Whether to show auto-stack buttons in the UI.")
        .build()).clientOnly().commit();

    this.sortMode = this.buildRegistration(EnumConfigOption.builder(ConfigPath.of("sorting", "sortMode"),
            List.of(SortMode.values()))
        .setDefaultValue(SortMode.ALPHABETICAL)
        .setComment(
            "How items are ordered when sorting. 'alphabetical' sorts by item name; 'creative' uses creative-menu order.")
        .build()).clientOnly().commit();

    this.containersFirst = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
            "sorting", "containersFirst"))
        .setDefaultValue(false)
        .setComment(
            "Place items that hold contents (shulker boxes, bundles, etc.) before other items. Only applies in 'alphabetical' sort mode.")
        .build()).clientOnly().commit();

    this.defaultPosition = this.buildRegistration(PositionConfigOption.builder(ConfigPath.of(
            "sorting", "defaultPosition"))
        .setDefaultValue(new Position(-4, -1))
        .setComment("Customize a default for button position.")
        .onUpdate((option) -> option.setDisabled(
            !this.showSort.getValue() && !this.showTransfer.getValue() && !this.showStack.getValue()))
        .build()).clientOnly().commit();

    this.screenPositions = this.buildRegistration(PerScreenPositionConfigOption.builder(ConfigPath.of(
            "sorting", "screenPositions"))
        .setComment("Customize button position on a per-screen basis.")
        .build()).clientOnly().noGuiControl().commit();

    this.enableSlotLocking = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
            "sorting", "enableSlotLocking"))
        .setDefaultValue(true)
        .setComment(
            "Master toggle for locking player-inventory slots (Ctrl+click). When false, locking is disabled, no markers/tooltips are drawn, and locked slots are no longer skipped by sort/auto-stack/transfer. Stored locks are kept for when it is re-enabled.")
        .build()).clientOnly().commit();

    this.lockedSlotDisplay = this.buildRegistration(EnumConfigOption.builder(ConfigPath.of(
            "sorting", "lockedSlotDisplay"), List.of(LockedSlotDisplay.values()))
        .setDefaultValue(LockedSlotDisplay.getDefault())
        .setComment(
            "When to draw the locked-slot marker (border + darkened background). 'shown' always draws it; 'hidden' never does; 'hotkey' only while the 'Peek locked slots' keybind is held.")
        .onUpdate((option) -> option.setDisabled(!this.enableSlotLocking.getValue()))
        .build()).clientOnly().commit();

    this.serverLockedPlayerSlots = this.buildRegistration(ServerLockedSlotsConfigOption.builder(ConfigPath.of(
            "sorting", "serverLockedPlayerSlots"))
        .setComment(
            "Per-server locked player-inventory slot indices, keyed by server address. Single-player locks are stored per-save in the world config instead. Managed programmatically; no GUI control.")
        .build()).clientOnly().noGuiControl().commit();

    // ----- Sorting Groups group ("grouping") -----

    this.itemGrouping = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("grouping", "itemGrouping"))
        .setDefaultValue(true)
        .setComment(
            "Group color/variant families of items together (e.g. all wool colors, all terracotta) instead of sorting each variant purely by name.")
        .build()).clientOnly().commit();

    this.dynamicGroupsEnabled = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of(
            "grouping", "dynamicGroups"))
        .setDefaultValue(true)
        .setComment(
            "Group items together based on datapack-defined item tags under the 'grouping/' folder (e.g. #mymod:grouping/gems). Master switch for all pack-defined grouping families.")
        .build()).clientOnly().commit();

    this.disabledDynamicGroups = this.buildRegistration(StringListConfigOption.builder(ConfigPath.of(
            "grouping", "disabledDynamicGroups"))
        .setComment(
            "Individually disabled datapack grouping families, by full tag id (e.g. mymod:grouping/gems). Managed programmatically; no GUI control.")
        .build()).clientOnly().noGuiControl().commit();

    // Per-family grouping toggles, one default-true option per GroupDefs.ALL entry, all nested
    // under the "grouping" group so they render as a single GUI section. Rebuilt every time
    // registerOptions() runs (init + syncWithStore after clear()).
    this.groupToggles.clear();
    for (GroupDefs.GroupDef def : GroupDefs.ALL) {
      this.groupToggles.put(def.id(), this.buildRegistration(BooleanConfigOption.builder(
              ConfigPath.of("grouping", def.id()))
          .setDefaultValue(true)
          .setComment("Group " + def.displayName() + " variants together when sorting.")
          .build()).clientOnly().commit());
    }
  }

  /**
   * Live, isInitialized-guarded supplier for a grouping family's enabled state. Looks up the
   * current {@link #groupToggles} entry by id on each call so it stays correct across
   * {@link #syncWithStore()} rebuilds. Defaults to {@code true} before init and is false-safe if
   * the id is unknown.
   */
  public BooleanSupplier groupEnabled(String id) {
    return () -> {
      if (!this.isInitialized()) {
        return true;
      }
      BooleanConfigOption option = this.groupToggles.get(id);
      return option != null && option.getValue();
    };
  }

  /**
   * Live, isInitialized-guarded supplier for a data-driven (datapack tag) grouping family's enabled
   * state, keyed by the family's full tag-id string. Returns {@code true} before init; otherwise the
   * family is enabled only when the {@link #dynamicGroupsEnabled} master gate is on AND the id is not
   * present in {@link #disabledDynamicGroups}. Because enablement is keyed by persisted strings (not
   * by a live option instance), it survives {@link #syncWithStore()} rebuilds and is inert for ids
   * whose pack has been removed.
   */
  public BooleanSupplier groupEnabledDynamic(String fullTagId) {
    return () -> {
      if (!this.isInitialized()) {
        return true;
      }
      return this.dynamicGroupsEnabled.getValue() && !this.disabledDynamicGroups.getValue().contains(fullTagId);
    };
  }

  /**
   * The current set of locked player main-inventory slot indices for wherever the player is right
   * now: the per-save {@link InventoryManagementWorldConfig} in single-player, or this config's
   * per-server {@link #serverLockedPlayerSlots} map (keyed by server address) on a multiplayer
   * server. Returns an empty list before init, with no world loaded, or when the multiplayer server
   * address is unavailable. The committed value tracks the pending one because
   * {@link #toggleLockedPlayerSlot(int)} persists immediately.
   */
  public List<Integer> getLockedPlayerSlots() {
    if (!this.isInitialized()) {
      return List.of();
    }
    if (hostsWorld()) {
      return InventoryManagementWorldConfig.getInstance().getLockedPlayerSlots();
    }
    String key = currentServerKey();
    return key == null ? List.of() : this.serverLockedPlayerSlots.get(key);
  }

  /**
   * The current locked-slot marker display mode, guarded against pre-init access (returns the default,
   * {@link LockedSlotDisplay#SHOWN}, before the config is initialized).
   */
  public LockedSlotDisplay getLockedSlotDisplay() {
    if (!this.isInitialized() || this.lockedSlotDisplay == null) {
      return LockedSlotDisplay.getDefault();
    }
    return this.lockedSlotDisplay.getValue();
  }

  /**
   * Whether the Locked Slots feature is active, guarded against pre-init access (returns the default,
   * {@code true}, before the config is initialized). The single gate for every locked-slot behavior:
   * the Ctrl+click toggle and marker/tooltip rendering in {@code SlotLockMixin}, and the wire mask
   * built in {@code ClientNetworking}. When false, stored locks are untouched but inert.
   */
  public boolean isSlotLockingEnabled() {
    return !this.isInitialized() || this.enableSlotLocking == null || this.enableSlotLocking.getValue();
  }

  /**
   * The configured percent-of-max durability alert thresholds, guarded against pre-init access (returns
   * an empty list before the config is initialized). Mirrors {@link #getLockedPlayerSlots()}.
   */
  public List<Integer> getDurabilityAlertThresholds() {
    if (!this.isInitialized() || this.durabilityAlertThresholds == null) {
      return List.of();
    }
    return this.durabilityAlertThresholds.getValue();
  }

  /**
   * Flips {@code slot}'s locked state for the current world: the per-save
   * {@link InventoryManagementWorldConfig} in single-player, or this config's per-server
   * {@link #serverLockedPlayerSlots} map on a multiplayer server (each persists immediately to its
   * own store). No-op when the multiplayer server address is unavailable. Intended for user-driven
   * actions only (a single Ctrl+click), not high-frequency loops.
   */
  public void toggleLockedPlayerSlot(int slot) {
    if (hostsWorld()) {
      InventoryManagementWorldConfig.getInstance().toggleLockedPlayerSlot(slot);
      return;
    }
    String key = currentServerKey();
    if (key == null) {
      return;
    }
    this.serverLockedPlayerSlots.toggle(key, slot);
    this.writeToStore();
  }

  /**
   * Whether we host the loaded world in-process — true single-player or the host of a LAN game — so
   * the per-save {@link WorldScopedFileStore world store} is writable. Everything else (dedicated
   * servers, direct-connect, LAN clients) is a remote connection that uses the per-server map. This
   * is exactly the condition under which {@link InventoryManagementWorldConfig}'s store is ready, so
   * the two never disagree.
   */
  private static boolean hostsWorld() {
    return PathAccessor.get().isWorldDirAccessible();
  }

  /**
   * Per-server lock map key for the connected server, or null if none (e.g. main menu, or a LAN
   * world with no server entry). The server address is sanitized to a TOML-safe bare key
   * (dots/colons → {@code _}), matching the convention {@link PerScreenPositionConfigOption} uses for
   * its screen keys so the map serializes cleanly.
   */
  private static String currentServerKey() {
    ServerData server = Minecraft.getInstance().getCurrentServer();
    if (server == null || server.ip == null) {
      return null;
    }
    return server.ip.replaceAll("[^A-Za-z0-9_-]", "_");
  }
}
