package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToolComparatorTest extends BaseMinecraftTest {
  private static ToolComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new ToolComparator();
  }

  @ParameterizedTest
  @MethodSource("getNonToolSamples")
  void ignoresNonToolItems(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getNonToolSamples() {
    return getUniquePairs(List.of(
        new ItemStack(Items.DIAMOND),
        new ItemStack(Items.DIRT),
        new ItemStack(Items.BANNER.red())
    ));
  }

  @Test
  void sortsByMiningSpeedDesc() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createToolStack(10.0f),
        createToolStack(5.0f),
        createToolStack(1.0f)
    ));
  }

  @Test
  void equalSpeedsAreEqual() {
    assertEquals(0, comparator.compare(createToolStack(5.0f), createToolStack(5.0f)));
  }

  private static ItemStack createToolStack(float miningSpeed) {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    stack.set(DataComponents.TOOL, new Tool(List.of(), miningSpeed, 1, true));
    return stack;
  }
}
