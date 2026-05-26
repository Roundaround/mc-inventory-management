package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.selectCounts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class CountComparatorTest extends BaseMinecraftTest {
  private static CountComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new CountComparator();
  }

  @ParameterizedTest
  @MethodSource("getMiscSamples")
  void ignoresActualItem(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getMiscSamples() {
    return getUniquePairs(List.of(
        new ItemStack(Items.NETHERITE_CHESTPLATE),
        new ItemStack(Items.RED_BANNER),
        new ItemStack(Items.DIAMOND_CHESTPLATE),
        new ItemStack(Items.FIRE_CHARGE),
        new ItemStack(Items.BLUE_BANNER),
        new ItemStack(Items.BAMBOO)
    ));
  }

  @Test
  void sortsCountDesc() {
    ArrayList<ItemStack> actual = Lists.newArrayList(
        new ItemStack(Items.DIAMOND, 16),
        new ItemStack(Items.DIAMOND, 48),
        new ItemStack(Items.DIAMOND, 64)
    );

    actual.sort(comparator);

    assertIterableEquals(List.of(64, 48, 16), selectCounts(actual));
  }

  @Test
  void ignoresMaxCount() {
    ArrayList<ItemStack> actual = Lists.newArrayList(
        new ItemStack(Items.DIAMOND_SWORD, 16),
        new ItemStack(Items.SNOWBALL, 48),
        new ItemStack(Items.DIAMOND, 64)
    );

    actual.sort(comparator);

    assertIterableEquals(List.of(64, 48, 16), selectCounts(actual));
  }
}
