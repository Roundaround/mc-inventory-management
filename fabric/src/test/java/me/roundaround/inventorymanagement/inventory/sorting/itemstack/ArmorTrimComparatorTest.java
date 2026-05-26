package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.createListOfEmpty;
import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArmorTrimComparatorTest extends BaseMinecraftTest {
  private static ArmorTrimComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new ArmorTrimComparator();
  }

  @ParameterizedTest
  @MethodSource("getUntrimmedSamples")
  void ignoresItemsWithoutTrim(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getUntrimmedSamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.TRIM,
        Items.DIAMOND_CHESTPLATE,
        Items.IRON_CHESTPLATE,
        Items.GOLDEN_CHESTPLATE
    ));
  }

  @Test
  void untrimmedItemsAreEqual() {
    ItemStack a = new ItemStack(Items.DIAMOND_CHESTPLATE);
    ItemStack b = new ItemStack(Items.IRON_CHESTPLATE);
    assertEquals(0, comparator.compare(a, b));
  }
}
