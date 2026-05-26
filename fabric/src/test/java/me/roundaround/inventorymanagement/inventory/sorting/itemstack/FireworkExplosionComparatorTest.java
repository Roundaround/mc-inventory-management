package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
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

public class FireworkExplosionComparatorTest extends BaseMinecraftTest {
  private static FireworkExplosionComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new FireworkExplosionComparator();
  }

  @ParameterizedTest
  @MethodSource("getEmptySamples")
  void ignoresItemsWithoutComponent(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getEmptySamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.FIREWORK_EXPLOSION,
        Items.DIAMOND,
        Items.DIRT,
        Items.BAMBOO
    ));
  }

  @Test
  void sortsByShapeIndex() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createFireworkStar(FireworkExplosion.Shape.SMALL_BALL),
        createFireworkStar(FireworkExplosion.Shape.LARGE_BALL),
        createFireworkStar(FireworkExplosion.Shape.STAR),
        createFireworkStar(FireworkExplosion.Shape.CREEPER),
        createFireworkStar(FireworkExplosion.Shape.BURST)
    ));
  }

  @Test
  void sortsByEffects() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createFireworkStar(FireworkExplosion.Shape.SMALL_BALL, false, false),
        createFireworkStar(FireworkExplosion.Shape.SMALL_BALL, true, false),
        createFireworkStar(FireworkExplosion.Shape.SMALL_BALL, false, true),
        createFireworkStar(FireworkExplosion.Shape.SMALL_BALL, true, true)
    ));
  }

  private static ItemStack createFireworkStar(FireworkExplosion.Shape shape) {
    return createFireworkStar(shape, false, false);
  }

  private static ItemStack createFireworkStar(FireworkExplosion.Shape shape, boolean trail, boolean twinkle) {
    ItemStack stack = new ItemStack(Items.FIREWORK_STAR);
    stack.set(DataComponents.FIREWORK_EXPLOSION,
        new FireworkExplosion(shape, IntList.of(), IntList.of(), trail, twinkle));
    return stack;
  }
}
