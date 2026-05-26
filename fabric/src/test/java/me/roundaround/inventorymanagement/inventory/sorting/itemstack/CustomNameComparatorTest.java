package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomNameComparatorTest extends BaseMinecraftTest {
  private static CustomNameComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new CustomNameComparator();
  }

  @ParameterizedTest
  @MethodSource("getUnamedSamples")
  void ignoresItemsWithoutCustomName(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getUnamedSamples() {
    return getUniquePairs(List.of(
        new ItemStack(Items.DIAMOND),
        new ItemStack(Items.DIRT),
        new ItemStack(Items.NETHERITE_CHESTPLATE),
        new ItemStack(Items.BAMBOO)
    ));
  }

  @Test
  void sortsAlphabeticallyByCustomName() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createNamed(Items.DIAMOND, "Alpha"),
        createNamed(Items.DIAMOND, "Beta"),
        createNamed(Items.DIAMOND, "Gamma")
    ));
  }

  @Test
  void caseInsensitiveSorting() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createNamed(Items.DIAMOND, "apple"),
        createNamed(Items.DIAMOND, "Banana"),
        createNamed(Items.DIAMOND, "cherry")
    ));
  }

  @Test
  void namedItemsSortBeforeUnnamed() {
    CustomNameComparator comp = new CustomNameComparator();
    ItemStack named = createNamed(Items.DIAMOND, "Test");
    ItemStack unnamed = new ItemStack(Items.DIAMOND);

    assertTrue(comp.compare(named, unnamed) < 0, "Named items should sort before unnamed (nullsLast)");
    assertTrue(comp.compare(unnamed, named) > 0, "Unnamed items should sort after named (nullsLast)");
  }

  private static ItemStack createNamed(net.minecraft.world.item.Item item, String name) {
    ItemStack stack = new ItemStack(item);
    stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    return stack;
  }
}
