package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;

public class MusicDiscComparator extends CachingComparatorImpl<ItemStack, String> {
  public MusicDiscComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected String mapValue(ItemStack stack) {
    JukeboxPlayable playable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
    if (playable == null) {
      return null;
    }
    return playable.song().unwrapKey()
        .map(key -> key.identifier().toString())
        .orElse(null);
  }
}
