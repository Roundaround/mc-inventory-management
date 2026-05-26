package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;

public class MapIdComparator extends CachingComparatorImpl<ItemStack, Integer> {
  public MapIdComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected Integer mapValue(ItemStack stack) {
    MapId mapId = stack.get(DataComponents.MAP_ID);
    if (mapId == null) {
      return null;
    }
    return mapId.id();
  }
}
