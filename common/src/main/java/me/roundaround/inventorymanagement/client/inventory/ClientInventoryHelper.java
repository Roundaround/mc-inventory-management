package me.roundaround.inventorymanagement.client.inventory;

import me.roundaround.inventorymanagement.config.SortMode;
import me.roundaround.inventorymanagement.inventory.IgnoredSlots;
import me.roundaround.inventorymanagement.inventory.InventoryHelper;
import me.roundaround.inventorymanagement.inventory.SlotRange;
import me.roundaround.inventorymanagement.inventory.SortableInventory;
import me.roundaround.inventorymanagement.inventory.sorting.SortContext;
import me.roundaround.inventorymanagement.inventory.sorting.itemstack.ItemStackComparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ClientInventoryHelper {
  private ClientInventoryHelper() {
  }

  public static List<Integer> calculatePlayerSort(Player player, long lockedMask) {
    Container inventory = player.getInventory();
    SlotRange slotRange = IgnoredSlots.playerLockedRange(lockedMask);
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
    SortContext context = new SortContext(player.getUUID());
    if (context.mode() == SortMode.CREATIVE) {
      buildCreativeTabContents();
    }
    return new SortableInventory(inventory).sort(slotRange, ItemStackComparator.create(context));
  }

  /**
   * Vanilla only builds the creative tabs' display contents when the creative inventory screen opens, which a
   * survival player never does; build them here exactly as that screen does so creative order has something to
   * index. Same parameters as the screen, so the two never thrash vanilla's cache.
   */
  private static void buildCreativeTabContents() {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null) {
      return;
    }
    HolderLookup.Provider holders = player.level().registryAccess();
    boolean hasPermissions = player.canUseGameMasterBlocks() && mc.options.operatorItemsTab().get();
    if (!CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), hasPermissions, holders)) {
      return;
    }
    // the screen refreshes its search trees only when its own rebuild call wins; skip this and creative search goes empty
    List<ItemStack> searchItems = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
    player.connection.searchTrees().updateCreativeTooltips(holders, searchItems);
    player.connection.searchTrees().updateCreativeTags(searchItems);
  }
}
