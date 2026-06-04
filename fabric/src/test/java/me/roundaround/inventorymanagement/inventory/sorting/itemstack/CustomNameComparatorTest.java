package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

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
    ItemStack alpha = createNamed(Items.DIAMOND, "Alpha");
    ItemStack beta = createNamed(Items.DIAMOND, "Beta");
    ItemStack gamma = createNamed(Items.DIAMOND, "Gamma");

    assertTrue(comparator.compare(alpha, beta) < 0, "Alpha should sort before Beta");
    assertTrue(comparator.compare(beta, gamma) < 0, "Beta should sort before Gamma");
    assertTrue(comparator.compare(gamma, alpha) > 0, "Gamma should sort after Alpha");
    assertEquals(0, comparator.compare(alpha, createNamed(Items.DIAMOND, "Alpha")),
        "Equal custom names should compare as equal");
  }

  @Test
  void caseInsensitiveSorting() {
    ItemStack apple = createNamed(Items.DIAMOND, "apple");
    ItemStack banana = createNamed(Items.DIAMOND, "Banana");
    ItemStack cherry = createNamed(Items.DIAMOND, "cherry");

    // Under plain String::compareTo, uppercase 'B' (66) would sort before lowercase 'a' (97),
    // breaking apple < Banana. These assertions only pass with nullsLast(compareToIgnoreCase).
    assertTrue(comparator.compare(apple, banana) < 0, "apple should sort before Banana (case-insensitive)");
    assertTrue(comparator.compare(banana, cherry) < 0, "Banana should sort before cherry (case-insensitive)");
    assertEquals(0, comparator.compare(createNamed(Items.DIAMOND, "Same"), createNamed(Items.DIAMOND, "SAME")),
        "Names differing only in case should compare as equal");
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
