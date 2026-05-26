package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class ChargedProjectileComparator extends CachingComparatorImpl<ItemStack,
    ChargedProjectileComparator.ChargeSummary> {
  public ChargedProjectileComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected ChargeSummary mapValue(ItemStack stack) {
    return ChargeSummary.of(stack);
  }

  protected record ChargeSummary(boolean loaded, int projectileCount,
                                 String projectileType) implements Comparable<ChargeSummary> {
    private static Comparator<ChargeSummary> comparator;

    public static ChargeSummary of(ItemStack stack) {
      ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
      if (charged == null) {
        return null;
      }

      boolean loaded = !charged.isEmpty();
      int projectileCount = charged.itemCopies().size();
      String projectileType = charged.itemCopies().stream()
          .findFirst()
          .map(s -> s.getItem().getDescriptionId())
          .orElse("");

      return new ChargeSummary(loaded, projectileCount, projectileType);
    }

    @Override
    public int compareTo(@NotNull ChargeSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<ChargeSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparing(ChargeSummary::loaded).reversed(),
            Comparator.comparingInt(ChargeSummary::projectileCount).reversed(),
            Comparator.comparing(ChargeSummary::projectileType)
        );
      }
      return comparator;
    }
  }
}
