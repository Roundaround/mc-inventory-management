package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class ArmorTrimComparator extends CachingComparatorImpl<ItemStack, ArmorTrimComparator.TrimSummary> {
  public ArmorTrimComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected TrimSummary mapValue(ItemStack stack) {
    return TrimSummary.of(stack);
  }

  protected record TrimSummary(String patternKey, String materialKey) implements Comparable<TrimSummary> {
    private static Comparator<TrimSummary> comparator;

    public static TrimSummary of(ItemStack stack) {
      ArmorTrim trim = stack.get(DataComponents.TRIM);
      if (trim == null) {
        return null;
      }

      String patternKey = trim.pattern().unwrapKey()
          .map(key -> key.identifier().toString())
          .orElse("");
      String materialKey = trim.material().unwrapKey()
          .map(key -> key.identifier().toString())
          .orElse("");

      return new TrimSummary(patternKey, materialKey);
    }

    @Override
    public int compareTo(@NotNull TrimSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<TrimSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparing(TrimSummary::patternKey),
            Comparator.comparing(TrimSummary::materialKey)
        );
      }
      return comparator;
    }
  }
}
