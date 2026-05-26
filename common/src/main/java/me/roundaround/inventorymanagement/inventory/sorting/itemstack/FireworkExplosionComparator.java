package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import it.unimi.dsi.fastutil.ints.IntList;
import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.LexicographicalListComparator;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class FireworkExplosionComparator extends CachingComparatorImpl<ItemStack,
    FireworkExplosionComparator.ExplosionSummary> {
  public FireworkExplosionComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected ExplosionSummary mapValue(ItemStack stack) {
    return ExplosionSummary.of(stack);
  }

  public record ExplosionSummary(int index,
                                 int effects,
                                 List<String> colors,
                                 List<String> fades) implements Comparable<ExplosionSummary> {
    private static Comparator<ExplosionSummary> comparator;

    public static ExplosionSummary of(ItemStack stack) {
      return of(stack.get(DataComponents.FIREWORK_EXPLOSION));
    }

    public static ExplosionSummary of(FireworkExplosion component) {
      if (component == null) {
        return null;
      }

      int index = component.shape().ordinal();

      int effects = 0;
      if (component.hasTrail()) {
        effects += 1;
      }
      if (component.hasTwinkle()) {
        effects += 2;
      }

      Language language = Language.getInstance();
      List<String> colors = getColorNames(language, component.colors());
      List<String> fades = getColorNames(language, component.fadeColors());

      return new ExplosionSummary(index, effects, colors, fades);
    }

    @Override
    public int compareTo(@NotNull ExplosionSummary other) {
      return getComparator().compare(this, other);
    }

    public static Comparator<ExplosionSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(ExplosionSummary::index),
            Comparator.comparingInt(ExplosionSummary::effects),
            Comparator.comparing(ExplosionSummary::colors, LexicographicalListComparator.naturalOrder()),
            Comparator.comparing(ExplosionSummary::fades, LexicographicalListComparator.naturalOrder())
        );
      }
      return comparator;
    }

    public static List<String> getColorNames(Language language, IntList colors) {
      return List.copyOf(colors).stream().map((fireworkColor) -> {
        DyeColor dyeColor = DyeColor.byFireworkColor(fireworkColor);
        if (dyeColor == null) {
          return Integer.toString(fireworkColor);
        }
        return language.getOrDefault(String.format("color.minecraft.%s", dyeColor.getName()));
      }).sorted(Comparator.naturalOrder()).toList();
    }
  }

  public static class ByComponent extends CachingComparatorImpl<FireworkExplosion, ExplosionSummary> {
    public ByComponent() {
      super(PredicatedComparator.ignoreNullsNaturalOrder());
    }

    @Override
    protected ExplosionSummary mapValue(FireworkExplosion component) {
      return ExplosionSummary.of(component);
    }
  }
}
