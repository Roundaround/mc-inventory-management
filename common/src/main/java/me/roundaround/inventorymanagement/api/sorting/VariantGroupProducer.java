package me.roundaround.inventorymanagement.api.sorting;

import me.roundaround.inventorymanagement.inventory.sorting.SortContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiFunction;

@FunctionalInterface
public interface VariantGroupProducer extends BiFunction<SortContext, ItemStack, List<String>> {
}
