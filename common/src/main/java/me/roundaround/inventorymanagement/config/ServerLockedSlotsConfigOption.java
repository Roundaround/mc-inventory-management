package me.roundaround.inventorymanagement.config;

import me.roundaround.trove.config.ConfigPath;
import me.roundaround.trove.config.option.ConfigOption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-server locked player-inventory slots, keyed by multiplayer server address, persisted in the
 * global client config. Single-player locks live per-save in {@link InventoryManagementWorldConfig}
 * instead; this map only holds the dedicated-server entries. Serializes as a TOML table of
 * {@code "<server address>" = [<int>, ...]} (mirrors {@link PerScreenPositionConfigOption}).
 */
public class ServerLockedSlotsConfigOption extends ConfigOption<Map<String, List<Integer>>> {
  protected ServerLockedSlotsConfigOption(Builder builder) {
    super(builder);
  }

  /** The locked slots for {@code serverKey}, or an empty list if that server has none. */
  public List<Integer> get(String serverKey) {
    List<Integer> value = this.getPendingValue().get(serverKey);
    return value == null ? List.of() : value;
  }

  /**
   * Flips membership of {@code slot} for {@code serverKey}. A server whose set becomes empty is
   * dropped from the map so it leaves no residue. Does not persist on its own; the caller writes the
   * config.
   */
  public void toggle(String serverKey, int slot) {
    Map<String, List<Integer>> map = deepCopy(this.getPendingValue());
    List<Integer> slots = new ArrayList<>(map.getOrDefault(serverKey, List.of()));
    if (slots.contains(slot)) {
      slots.remove(Integer.valueOf(slot));
    } else {
      slots.add(slot);
    }
    if (slots.isEmpty()) {
      map.remove(serverKey);
    } else {
      map.put(serverKey, List.copyOf(slots));
    }
    this.setValue(map);
  }

  private static Map<String, List<Integer>> deepCopy(Map<String, List<Integer>> source) {
    Map<String, List<Integer>> copy = new LinkedHashMap<>();
    source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
    return copy;
  }

  @Override
  public void deserialize(Object data) {
    Map<String, List<Integer>> deserialized = new LinkedHashMap<>();
    if (data instanceof Map<?, ?> mapData) {
      mapData.forEach((key, value) -> {
        if (value instanceof List<?> listValue) {
          List<Integer> ints = listValue.stream()
              .map((entry) -> entry instanceof Number number ?
                  number.intValue() :
                  Integer.parseInt(String.valueOf(entry)))
              .toList();
          deserialized.put((String) key, ints);
        }
      });
    }
    this.setValue(deserialized);
  }

  @Override
  public Object serialize() {
    Map<String, Object> serialized = new LinkedHashMap<>();
    this.getPendingValue().forEach((key, value) -> serialized.put(key, List.copyOf(value)));
    return serialized;
  }

  public static Builder builder(ConfigPath path) {
    return new Builder(path);
  }

  public static class Builder extends ConfigOption.AbstractBuilder<Map<String, List<Integer>>,
      ServerLockedSlotsConfigOption, Builder> {
    private Builder(ConfigPath path) {
      super(path);
      this.setDefaultValue(new LinkedHashMap<>());
    }

    @Override
    protected ServerLockedSlotsConfigOption buildInternal() {
      return new ServerLockedSlotsConfigOption(this);
    }
  }
}
