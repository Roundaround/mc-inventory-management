package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.LexicographicalListComparator;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BannerComparator extends CachingComparatorImpl<ItemStack, BannerComparator.BannerSummary> {
  public BannerComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected BannerSummary mapValue(ItemStack stack) {
    return BannerSummary.of(stack);
  }

  protected record BannerSummary(int count, List<String> translated) implements Comparable<BannerSummary> {
    private static Comparator<BannerSummary> comparator;

    public static BannerSummary of(ItemStack stack) {
      BannerPatternLayers component = stack.get(DataComponents.BANNER_PATTERNS);
      if (component == null) {
        return null;
      }

      int count = component.layers().size();
      ArrayList<String> translated = new ArrayList<>(2 * count);

      Language language = Language.getInstance();
      for (var layer : component.layers()) {
        String i18nKey = layer.pattern().value().translationKey() + "." + layer.color().getName();
        translated.add(language.getOrDefault(i18nKey));
      }

      return new BannerSummary(count, translated);
    }

    @Override
    public int compareTo(@NotNull BannerSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<BannerSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(BannerSummary::count),
            Comparator.comparing(BannerSummary::translated, LexicographicalListComparator.naturalOrder())
        );
      }
      return comparator;
    }
  }
}
