package me.roundaround.inventorymanagement.config;

import me.roundaround.trove.config.value.EnumValue;

import java.util.Arrays;

/**
 * Controls when the locked-slot marker (darkened background + border drawn under the item) is rendered
 * on locked player inventory slots. {@link #SHOWN} always draws it, {@link #HIDDEN} never does, and
 * {@link #HOTKEY} only draws it while the "Peek locked slots" keybind is physically held. Defaults to
 * {@link #SHOWN} so locked slots are visible out of the box. Locking behaviour (Ctrl+click) and the
 * hover tooltip are unaffected by this option.
 */
public enum LockedSlotDisplay implements EnumValue<LockedSlotDisplay> {
  SHOWN("shown"),
  HIDDEN("hidden"),
  HOTKEY("hotkey");

  private final String id;

  LockedSlotDisplay(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getI18nKey(String modId) {
    return modId + ".sorting.lockedSlotDisplay." + this.id;
  }

  @Override
  public LockedSlotDisplay getFromId(String id) {
    return fromId(id);
  }

  @Override
  public LockedSlotDisplay getNext() {
    return values()[(this.ordinal() + 1) % values().length];
  }

  @Override
  public LockedSlotDisplay getPrev() {
    return values()[(this.ordinal() + values().length - 1) % values().length];
  }

  public static LockedSlotDisplay getDefault() {
    return SHOWN;
  }

  public static LockedSlotDisplay fromId(String id) {
    return Arrays.stream(values()).filter((mode) -> mode.id.equals(id)).findFirst().orElse(getDefault());
  }
}
