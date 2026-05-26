package me.roundaround.inventorymanagement.inventory.sorting;

import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.SortMode;

import java.util.UUID;

public record SortContext(UUID player, SortMode mode, boolean containersFirst, boolean itemGrouping) {
  public SortContext(UUID player) {
    this(player, readMode(), readContainersFirst(), readItemGrouping());
  }

  private static SortMode readMode() {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    return config.isInitialized() ? config.sortMode.getValue() : SortMode.getDefault();
  }

  private static boolean readContainersFirst() {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    return config.isInitialized() ? config.containersFirst.getValue() : false;
  }

  private static boolean readItemGrouping() {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    return config.isInitialized() ? config.itemGrouping.getValue() : true;
  }
}
