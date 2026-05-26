package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.WrapperComparatorImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class PaintingComparator extends WrapperComparatorImpl<ItemStack> {
  public PaintingComparator() {
    super(Comparator.comparing(
        PaintingComparator::getVariantKey,
        Comparator.nullsLast(String::compareToIgnoreCase)
    ));
  }

  private static String getVariantKey(ItemStack stack) {
    Holder<PaintingVariant> variant = stack.get(DataComponents.PAINTING_VARIANT);
    if (variant == null) {
      return null;
    }
    return variant.unwrapKey().map(key -> key.identifier().toString()).orElse(null);
  }
}
