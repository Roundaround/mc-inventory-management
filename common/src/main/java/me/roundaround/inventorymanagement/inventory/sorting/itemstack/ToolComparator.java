package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public class ToolComparator extends CachingComparatorImpl<ItemStack, ToolComparator.ToolSummary> {
  public ToolComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected ToolSummary mapValue(ItemStack stack) {
    return ToolSummary.of(stack);
  }

  protected record ToolSummary(int attackDamage, int miningSpeed) implements Comparable<ToolSummary> {
    private static Comparator<ToolSummary> comparator;

    public static ToolSummary of(ItemStack stack) {
      Tool tool = stack.get(DataComponents.TOOL);
      if (tool == null) {
        return null;
      }

      int attackDamage = Optional.ofNullable(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))
          .orElse(ItemAttributeModifiers.EMPTY)
          .modifiers()
          .stream()
          .filter((entry) -> entry.matches(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID))
          .findFirst()
          .map((entry) -> (int) (entry.modifier().amount() * 100))
          .orElse(0);

      int miningSpeed = (int) (tool.defaultMiningSpeed() * 100);

      return new ToolSummary(attackDamage, miningSpeed);
    }

    @Override
    public int compareTo(@NotNull ToolSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<ToolSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(ToolSummary::attackDamage).reversed(),
            Comparator.comparingInt(ToolSummary::miningSpeed).reversed()
        );
      }
      return comparator;
    }
  }
}
