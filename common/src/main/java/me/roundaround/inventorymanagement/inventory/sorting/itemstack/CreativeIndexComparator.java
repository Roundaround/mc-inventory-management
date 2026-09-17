package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Orders stacks by creative-menu position: category tab in registry order, then position within the tab, with
 * items in no tab last. Indexes the tabs' display contents as they are at construction, so build one per sort
 * after {@code ClientInventoryHelper} has built the contents for the player; vanilla itself only builds them
 * when the creative inventory screen opens, and against empty tabs every item ties.
 */
public class CreativeIndexComparator implements Comparator<ItemStack> {
  private static final Comparator<TabPosition> BY_POSITION =
      Comparator.comparingInt(TabPosition::tab).thenComparingInt(TabPosition::index);

  private final Map<Item, TabPosition> positions = new HashMap<>();

  public CreativeIndexComparator() {
    int tab = 0;
    for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
      if (group.getType() != CreativeModeTab.Type.CATEGORY) {
        continue;
      }
      int index = 0;
      HashSet<Item> seen = new HashSet<>();
      for (ItemStack stack : group.getDisplayItems()) {
        if (seen.add(stack.getItem())) {
          this.positions.putIfAbsent(stack.getItem(), new TabPosition(tab, index++));
        }
      }
      tab++;
    }
  }

  @Override
  public int compare(ItemStack a, ItemStack b) {
    return Comparator.nullsLast(BY_POSITION).compare(this.positionOrNull(a), this.positionOrNull(b));
  }

  private TabPosition positionOrNull(ItemStack stack) {
    return this.positions.get(stack.getItem());
  }

  private record TabPosition(int tab, int index) {
  }
}
