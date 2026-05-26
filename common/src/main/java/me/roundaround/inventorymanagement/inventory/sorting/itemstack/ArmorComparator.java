package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.PredicatedComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public class ArmorComparator extends CachingComparatorImpl<ItemStack, ArmorComparator.ArmorSummary> {
  public ArmorComparator() {
    super(PredicatedComparator.ignoreNullsNaturalOrder());
  }

  @Override
  protected ArmorSummary mapValue(ItemStack stack) {
    return ArmorSummary.of(stack);
  }

  protected record ArmorSummary(int slotOrder, int armorValue) implements Comparable<ArmorSummary> {
    private static Comparator<ArmorSummary> comparator;

    public static ArmorSummary of(ItemStack stack) {
      Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
      if (equippable == null) {
        return null;
      }

      EquipmentSlot slot = equippable.slot();
      int slotOrder = slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? 10 + slot.getIndex() : slot.getIndex();

      int armorValue = Optional.ofNullable(stack.get(DataComponents.ATTRIBUTE_MODIFIERS))
          .orElse(ItemAttributeModifiers.EMPTY)
          .modifiers()
          .stream()
          .filter((entry) -> {
            Identifier id = Identifier.withDefaultNamespace("armor." + ArmorType.BODY.getName());
            return entry.matches(Attributes.ARMOR, id);
          })
          .findFirst()
          .map((entry) -> (int) (entry.modifier().amount() + 100))
          .orElse(0);

      return new ArmorSummary(slotOrder, armorValue);
    }

    @Override
    public int compareTo(@NotNull ArmorSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<ArmorSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(ArmorSummary::slotOrder).reversed(),
            Comparator.comparingInt(ArmorSummary::armorValue).reversed()
        );
      }
      return comparator;
    }
  }
}
