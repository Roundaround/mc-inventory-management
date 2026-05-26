package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.InstrumentComponent;

import java.util.Optional;

public class InstrumentComparator extends CachingComparatorImpl<ItemStack, String> {
  public InstrumentComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected String mapValue(ItemStack stack) {
    return Optional.ofNullable(stack.get(DataComponents.INSTRUMENT))
        .map(InstrumentComponent::instrument)
        .flatMap(holder -> holder.unwrapKey())
        .map((key) -> Language.getInstance().getOrDefault(Util.makeDescriptionId("instrument", key.identifier())))
        .orElse(null);
  }
}
