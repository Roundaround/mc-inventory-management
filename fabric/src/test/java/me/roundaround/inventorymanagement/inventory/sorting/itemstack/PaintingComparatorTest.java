package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaintingComparatorTest extends BaseMinecraftTest {
  private static PaintingComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new PaintingComparator();
  }

  @ParameterizedTest
  @MethodSource("getNonPaintingSamples")
  void ignoresNonPaintingItems(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getNonPaintingSamples() {
    return getUniquePairs(List.of(
        new ItemStack(Items.DIAMOND),
        new ItemStack(Items.DIRT),
        new ItemStack(Items.NETHERITE_CHESTPLATE)
    ));
  }

  @Test
  void paintingsWithoutVariantAreEqual() {
    ItemStack a = new ItemStack(Items.PAINTING);
    ItemStack b = new ItemStack(Items.PAINTING);
    assertEquals(0, comparator.compare(a, b));
  }
}
