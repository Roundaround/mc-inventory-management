package me.roundaround.inventorymanagement.client.inventory;

import me.roundaround.inventorymanagement.inventory.InventoryHelper;
import me.roundaround.inventorymanagement.inventory.SlotRange;
import me.roundaround.inventorymanagement.inventory.SortableInventory;
import me.roundaround.inventorymanagement.inventory.sorting.itemstack.ItemStackComparator;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class ClientInventoryHelper {
  private ClientInventoryHelper() {
  }

  public static List<Integer> calculatePlayerSort(Player player) {
    Container inventory = player.getInventory();
    SlotRange slotRange = SlotRange.playerMainRange();
    return calculateSort(player, inventory, slotRange);
  }

  public static List<Integer> calculateContainerSort(Player player) {
    Container inventory = InventoryHelper.getContainerInventory(player);
    if (inventory == null) {
      return List.of();
    }

    SlotRange slotRange = SlotRange.fullRange(inventory);
    return calculateSort(player, inventory, slotRange);
  }

  private static List<Integer> calculateSort(Player player, Container inventory, SlotRange slotRange) {
    return new SortableInventory(inventory).sort(slotRange, ItemStackComparator.create(player.getUUID()));
  }
}
