package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.LexicographicalListComparator;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class SuspiciousStewComparator extends CachingComparatorImpl<ItemStack,
    List<SuspiciousStewComparator.EffectSummary>> {
  public SuspiciousStewComparator() {
    super(PredicatedComparator.ignoreNulls(
        SerialComparator.comparing(
            Comparator.comparing(List::size, Comparator.reverseOrder()),
            LexicographicalListComparator.naturalOrder()
        )
    ));
  }

  @Override
  protected List<EffectSummary> mapValue(ItemStack stack) {
    SuspiciousStewEffects component = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
    if (component == null) {
      return null;
    }
    return component.effects()
        .stream()
        .map(EffectSummary::of)
        .sorted(PredicatedComparator.ignoreNullsNaturalOrder())
        .toList();
  }

  protected record EffectSummary(String translated, int duration) implements Comparable<EffectSummary> {
    private static Comparator<EffectSummary> comparator;

    public static EffectSummary of(SuspiciousStewEffects.Entry effect) {
      MobEffectInstance instance = effect.createEffectInstance();
      return new EffectSummary(
          Language.getInstance().getOrDefault(instance.getDescriptionId()),
          effect.duration()
      );
    }

    @Override
    public int compareTo(@NotNull EffectSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<EffectSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparing(EffectSummary::translated, Comparator.naturalOrder()),
            Comparator.comparingInt(EffectSummary::duration).reversed()
        );
      }
      return comparator;
    }
  }
}
