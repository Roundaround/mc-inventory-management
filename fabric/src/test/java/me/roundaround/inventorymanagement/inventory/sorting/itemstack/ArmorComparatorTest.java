package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArmorComparatorTest extends BaseMinecraftTest {
  private static ArmorComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new ArmorComparator();
  }

  @ParameterizedTest
  @MethodSource("getNonArmorSamples")
  void ignoresNonArmorItems(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getNonArmorSamples() {
    return getUniquePairs(List.of(
        new ItemStack(Items.DIAMOND),
        new ItemStack(Items.DIRT),
        new ItemStack(Items.BANNER.red())
    ));
  }

  @Test
  void headSlotSortsBeforeChest() {
    ItemStack head = createArmor(EquipmentSlot.HEAD);
    ItemStack chest = createArmor(EquipmentSlot.CHEST);
    assertTrue(comparator.compare(head, chest) < 0);
  }

  @Test
  void humanoidArmorSortsBeforeAnimalArmor() {
    ItemStack humanoid = createArmor(EquipmentSlot.CHEST);
    ItemStack animal = createArmor(EquipmentSlot.BODY);
    assertTrue(comparator.compare(humanoid, animal) < 0);
  }

  @Test
  void sameSlotSameValueAreEqual() {
    assertEquals(0, comparator.compare(createArmor(EquipmentSlot.CHEST), createArmor(EquipmentSlot.CHEST)));
  }

  private static ItemStack createArmor(EquipmentSlot slot) {
    ItemStack stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
    stack.set(DataComponents.EQUIPPABLE, Equippable.builder(slot).build());
    return stack;
  }
}
