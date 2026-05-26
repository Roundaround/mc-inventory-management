package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.api.sorting.ItemVariantRegistry;
import me.roundaround.inventorymanagement.api.sorting.VariantGroup;
import me.roundaround.inventorymanagement.inventory.sorting.*;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class ItemNameComparator extends CachingComparatorImpl<ItemStack, List<String>> {
  private final SortContext parameters;

  public ItemNameComparator(SortContext parameters) {
    super(LexicographicalListComparator.comparing(
        SerialComparator.comparing(
            Comparator.comparing(String::isEmpty).reversed(),
            PredicatedComparator.of(
                (name) -> !name.isEmpty(),
                String::compareToIgnoreCase
            )
        )
    ));
    this.parameters = parameters;
  }

  @Override
  protected List<String> mapValue(ItemStack stack) {
    return this.mapToTranslationKeys(stack).stream().map(ItemNameComparator::translate).toList();
  }

  private List<String> mapToTranslationKeys(ItemStack stack) {
    if (!this.parameters.itemGrouping()) {
      return List.of(getDescriptionId(stack));
    }

    for (VariantGroup group : ItemVariantRegistry.effectiveGroups()) {
      if (!group.enabled().getAsBoolean()) {
        continue;
      }
      if (group.predicate().test(stack)) {
        return group.groupProducer().apply(this.parameters, stack);
      }
    }

    return List.of(getDescriptionId(stack));
  }

  private static String getDescriptionId(ItemStack stack) {
    if (stack.isEmpty()) {
      return "";
    }
    return stack.getItem().getDescriptionId();
  }

  private static String translate(String i18nKey) {
    Language lang = Language.getInstance();
    return lang.getOrDefault(i18nKey + ".sort", lang.getOrDefault(i18nKey));
  }
}
