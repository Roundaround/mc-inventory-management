package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerFirstComparatorTest extends BaseMinecraftTest {
  private static ContainerFirstComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new ContainerFirstComparator();
  }

  @ParameterizedTest
  @MethodSource("getNonContainerSamples")
  void treatsNonContainerItemsAsEqual(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getNonContainerSamples() {
    return getUniquePairs(List.of(
        new ItemStack(Items.DIAMOND),
        new ItemStack(Items.DIRT),
        new ItemStack(Items.NETHERITE_CHESTPLATE),
        new ItemStack(Items.BAMBOO)
    ));
  }

  @Test
  void containerItemsSortBeforeNonContainer() {
    ItemStack container = new ItemStack(Items.SHULKER_BOX);
    container.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    ItemStack regular = new ItemStack(Items.DIAMOND);

    assertTrue(comparator.compare(container, regular) < 0);
    assertTrue(comparator.compare(regular, container) > 0);
  }

  @Test
  void preservesOrderWithContainersFirst() {
    ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
    shulker.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
    ItemStack chest = new ItemStack(Items.CHEST);

    assertPreservesOrder(comparator, Lists.newArrayList(shulker, chest));
  }
}
