package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.LexicographicalListComparator;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DecoratedPotComparator extends CachingComparatorImpl<ItemStack,
    DecoratedPotComparator.DecoratedPotSummary> {
  public DecoratedPotComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected DecoratedPotSummary mapValue(ItemStack stack) {
    return DecoratedPotSummary.of(stack);
  }

  protected record DecoratedPotSummary(int count, List<String> translated) implements Comparable<DecoratedPotSummary> {
    private static Comparator<DecoratedPotSummary> comparator;

    public static DecoratedPotSummary of(ItemStack stack) {
      PotDecorations component = stack.get(DataComponents.POT_DECORATIONS);
      if (component == null) {
        return null;
      }

      List<Item> items = component.ordered();

      int count = 0;
      ArrayList<String> translated = new ArrayList<>();

      Language language = Language.getInstance();
      for (Item item : items) {
        if (item != Items.BRICK) {
          count++;
          translated.add(language.getOrDefault(item.getDescriptionId()));
        } else {
          translated.add(null);
        }
      }

      return new DecoratedPotSummary(count, translated);
    }

    @Override
    public int compareTo(@NotNull DecoratedPotSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<DecoratedPotSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(DecoratedPotSummary::count),
            Comparator.comparing(
                DecoratedPotSummary::translated,
                LexicographicalListComparator.comparing(Comparator.nullsLast(String::compareToIgnoreCase))
            ));
      }
      return comparator;
    }
  }
}
