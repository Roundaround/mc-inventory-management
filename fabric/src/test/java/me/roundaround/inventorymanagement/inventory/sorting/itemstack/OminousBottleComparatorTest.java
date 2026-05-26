package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OminousBottleComparatorTest extends BaseMinecraftTest {
  private static OminousBottleComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new OminousBottleComparator();
  }

  @Test
  void ignoresNonBottleItems() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void sortsAmplifierDescending() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createBottle(4),
        createBottle(3),
        createBottle(2),
        createBottle(1),
        createBottle(0)
    ));
  }

  @Test
  void sameAmplifierAreEqual() {
    assertEquals(0, comparator.compare(createBottle(2), createBottle(2)));
  }

  private static ItemStack createBottle(int amplifier) {
    ItemStack stack = new ItemStack(Items.OMINOUS_BOTTLE);
    stack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(amplifier));
    return stack;
  }
}
