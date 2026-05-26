package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.createListOfEmpty;
import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainerContentsComparatorTest extends BaseMinecraftTest {
  private static ContainerContentsComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new ContainerContentsComparator();
  }

  @ParameterizedTest
  @MethodSource("getEmptySamples")
  void ignoresItemsWithoutComponent(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getEmptySamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.CONTAINER,
        Items.NETHERITE_CHESTPLATE,
        Items.RED_BANNER,
        Items.DIAMOND_CHESTPLATE,
        Items.FIRE_CHARGE,
        Items.SHULKER_BOX,
        Items.BLUE_BANNER,
        Items.BAMBOO
    ));
  }

  @ParameterizedTest
  @MethodSource("getShulkerSamples")
  void ignoresActualItem(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getShulkerSamples() {
    return getUniquePairs(List.of(
        createStack(Items.SHULKER_BOX, generateFullInventory(Items.DIRT, 27)),
        createStack(Items.BLUE_SHULKER_BOX, generateFullInventory(Items.DIRT, 27)),
        createStack(Items.ORANGE_SHULKER_BOX, generateFullInventory(Items.DIRT, 27)),
        createStack(Items.GREEN_SHULKER_BOX, generateFullInventory(Items.DIRT, 27))
    ));
  }

  @ParameterizedTest
  @MethodSource("getVaryingContentShulkerSamples")
  void ignoresActualContentItems(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getVaryingContentShulkerSamples() {
    return getUniquePairs(List.of(
        createStack(generateFullInventory(Items.DIRT, 27)),
        createStack(generateFullInventory(Items.DIAMOND, 27)),
        createStack(generateFullInventory(Items.MUD, 27)),
        createStack(generateFullInventory(Items.SAND, 27))
    ));
  }

  @Test
  void sortsDescendingBySlotsUsed() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createStack(generateFullInventory(Items.DIAMOND, 6)),
        createStack(generateFullInventory(Items.DIAMOND, 5)),
        createStack(generateFullInventory(Items.DIAMOND, 4)),
        createStack(generateFullInventory(Items.DIAMOND, 3)),
        createStack(generateFullInventory(Items.DIAMOND, 2)),
        createStack(generateFullInventory(Items.DIAMOND, 1))
    ));
  }

  @Test
  void sortsDescendingByTotalItemCountDesc() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createStack(List.of(
            new ItemStack(Items.DIAMOND, 16),
            new ItemStack(Items.DIAMOND, 64),
            new ItemStack(Items.DIAMOND, 16))),
        createStack(List.of(
            new ItemStack(Items.DIAMOND, 16),
            new ItemStack(Items.DIAMOND, 48),
            new ItemStack(Items.DIAMOND, 16))),
        createStack(List.of(
            new ItemStack(Items.DIAMOND, 16),
            new ItemStack(Items.DIAMOND, 16),
            new ItemStack(Items.DIAMOND, 16)))
    ));
  }

  private static ItemStack createStack(List<ItemStack> items) {
    return createStack(Items.SHULKER_BOX, items);
  }

  private static ItemStack createStack(Item container, List<ItemStack> items) {
    ItemStack stack = new ItemStack(container);
    stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    return stack;
  }

  private static List<ItemStack> generateFullInventory(Item item, int slots) {
    ArrayList<ItemStack> list = new ArrayList<>(slots);
    for (int i = 0; i < slots; i++) {
      ItemStack stack = new ItemStack(item, 64);
      list.add(stack);
    }
    return list;
  }
}
