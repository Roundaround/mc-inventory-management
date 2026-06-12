package me.roundaround.inventorymanagement.config;

import me.roundaround.trove.config.value.EnumValue;

import java.util.Arrays;

public enum SortMode implements EnumValue<SortMode> {
  ALPHABETICAL("alphabetical"),
  CREATIVE("creative");

  private final String id;

  SortMode(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getI18nKey(String modId) {
    return modId + ".sorting.sortMode." + this.id;
  }

  @Override
  public SortMode getFromId(String id) {
    return fromId(id);
  }

  @Override
  public SortMode getNext() {
    return values()[(this.ordinal() + 1) % values().length];
  }

  @Override
  public SortMode getPrev() {
    return values()[(this.ordinal() + values().length - 1) % values().length];
  }

  public static SortMode getDefault() {
    return ALPHABETICAL;
  }

  public static SortMode fromId(String id) {
    return Arrays.stream(values()).filter((mode) -> mode.id.equals(id)).findFirst().orElse(getDefault());
  }
}
