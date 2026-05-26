package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.createListOfEmpty;
import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecoratedPotComparatorTest extends BaseMinecraftTest {
  private static DecoratedPotComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new DecoratedPotComparator();
  }

  @ParameterizedTest
  @MethodSource("getEmptySamples")
  void ignoresItemsWithoutComponent(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getEmptySamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.POT_DECORATIONS,
        Items.DIAMOND,
        Items.DIRT,
        Items.BAMBOO
    ));
  }

  @Test
  void sortsBySherdCountAscending() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createPot(Items.ANGLER_POTTERY_SHERD, Items.BRICK, Items.BRICK, Items.BRICK),
        createPot(Items.ANGLER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD, Items.BRICK, Items.BRICK),
        createPot(Items.ANGLER_POTTERY_SHERD, Items.ARMS_UP_POTTERY_SHERD, Items.BLADE_POTTERY_SHERD, Items.BREWER_POTTERY_SHERD)
    ));
  }

  @Test
  void allBricksAreEqual() {
    ItemStack a = createPot(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);
    ItemStack b = createPot(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);
    assertEquals(0, comparator.compare(a, b));
  }

  private static ItemStack createPot(
      net.minecraft.world.item.Item back,
      net.minecraft.world.item.Item left,
      net.minecraft.world.item.Item right,
      net.minecraft.world.item.Item front
  ) {
    ItemStack stack = new ItemStack(Items.DECORATED_POT);
    stack.set(DataComponents.POT_DECORATIONS, new PotDecorations(back, left, right, front));
    return stack;
  }
}
