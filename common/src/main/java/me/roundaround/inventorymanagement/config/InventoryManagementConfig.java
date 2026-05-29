package me.roundaround.inventorymanagement.config;

import me.roundaround.inventorymanagement.api.sorting.GroupDefs;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.config.ConfigPath;
import me.roundaround.trove.config.manage.ModConfigImpl;
import me.roundaround.trove.config.manage.store.GameScopedFileStore;
import me.roundaround.trove.config.option.BooleanConfigOption;
import me.roundaround.trove.config.option.EnumConfigOption;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.option.StringListConfigOption;
import me.roundaround.trove.config.value.Position;

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
   * Absolute player main-inventory slot indices the user has locked. Locked slots are excluded from
   * sort, auto-stack, and transfer-all (both directions). Client-only and stateless on the server:
   * the list rides along inside each operation packet. Managed programmatically via
   * {@link #toggleLockedPlayerSlot(int)}; no GUI control.
   */
  public IntListConfigOption lockedPlayerSlots;

  /**
   * When the locked-slot marker (darkened background + border under the item) is drawn. Defaults to
   * {@link LockedSlotDisplay#SHOWN} — always visible; {@link LockedSlotDisplay#HOTKEY} restricts it to
   * while the "Peek locked slots" keybind is held. Rendered in the config GUI as an enum cycle control
   * (registered in {@code ConfigControlRegister}). Does not affect locking itself or the hover tooltip;
   * only the marker rendering in {@code SlotLockMixin}.
   */
  public EnumConfigOption<LockedSlotDisplay> lockedSlotDisplay;

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
    this.modEnabled = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("modEnabled"))
        .setDefaultValue(true)
        .setComment("Simple toggle for the mod! Set to false to disable.")
        .build()).clientOnly().commit();

    this.showSort = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("showSort"))
        .setDefaultValue(true)
        .setComment("Whether to show sort buttons in the UI.")
        .build()).clientOnly().commit();

    this.showTransfer = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("showTransfer"))
        .setDefaultValue(true)
        .setComment("Whether to show transfer buttons in the UI.")
        .build()).clientOnly().commit();

    this.showStack = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("showStack"))
        .setDefaultValue(true)
        .setComment("Whether to show auto-stack buttons in the UI.")
        .build()).clientOnly().commit();

    this.defaultPosition = this.buildRegistration(PositionConfigOption.builder(ConfigPath.of("defaultPosition"))
        .setDefaultValue(new Position(-4, -1))
        .setComment("Customize a default for button position.")
        .onUpdate((option) -> option.setDisabled(
            !this.showSort.getValue() && !this.showTransfer.getValue() && !this.showStack.getValue()))
        .build()).clientOnly().commit();

    this.sortMode = this.buildRegistration(EnumConfigOption.builder(ConfigPath.of("sortMode"),
            List.of(SortMode.values()))
        .setDefaultValue(SortMode.ALPHABETICAL)
        .setComment(
            "How items are ordered when sorting. 'alphabetical' sorts by item name; 'creative' uses creative-menu order.")
        .build()).clientOnly().commit();

    this.containersFirst = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("containersFirst"))
        .setDefaultValue(false)
        .setComment(
            "Place items that hold contents (shulker boxes, bundles, etc.) before other items. Only applies in 'alphabetical' sort mode.")
        .build()).clientOnly().commit();

    this.itemGrouping = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("itemGrouping"))
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

    this.screenPositions = this.buildRegistration(PerScreenPositionConfigOption.builder(ConfigPath.of(
            "screenPositions"))
        .setComment("Customize button position on a per-screen basis.")
        .build()).clientOnly().noGuiControl().commit();

    this.lockedPlayerSlots = this.buildRegistration(IntListConfigOption.builder(ConfigPath.of("lockedPlayerSlots"))
        .setComment(
            "Player main-inventory slot indices to lock from sorting, auto-stacking, and transfer-all. Managed programmatically; no GUI control.")
        .build()).clientOnly().noGuiControl().commit();

    this.lockedSlotDisplay = this.buildRegistration(EnumConfigOption.builder(ConfigPath.of("lockedSlotDisplay"),
            List.of(LockedSlotDisplay.values()))
        .setDefaultValue(LockedSlotDisplay.getDefault())
        .setComment(
            "When to draw the locked-slot marker (border + darkened background). 'shown' always draws it; 'hidden' never does; 'hotkey' only while the 'Peek locked slots' keybind is held.")
        .build()).clientOnly().commit();
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
   * The current set of locked player main-inventory slot indices. Returns the committed value, which
   * is kept in lockstep with the pending value because {@link #toggleLockedPlayerSlot(int)} persists
   * immediately. Returns an empty list before init.
   */
  public List<Integer> getLockedPlayerSlots() {
    if (!this.isInitialized()) {
      return List.of();
    }
    return this.lockedPlayerSlots.getValue();
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
   * Flips membership of {@code slot} in {@link #lockedPlayerSlots} and persists immediately. Intended
   * for user-driven actions only (a single Ctrl+click), not high-frequency loops.
   */
  public void toggleLockedPlayerSlot(int slot) {
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
