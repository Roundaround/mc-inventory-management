package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntList;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
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

public class FireworkRocketComparatorTest extends BaseMinecraftTest {
  private static FireworkRocketComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new FireworkRocketComparator();
  }

  @ParameterizedTest
  @MethodSource("getEmptySamples")
  void ignoresItemsWithoutComponent(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getEmptySamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.FIREWORKS,
        Items.DIAMOND,
        Items.DIRT,
        Items.BAMBOO
    ));
  }

  @Test
  void sortsByFlightDurationDesc() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createRocket(3),
        createRocket(2),
        createRocket(1)
    ));
  }

  @Test
  void sortsByExplosionCount() {
    FireworkExplosion explosion = new FireworkExplosion(
        FireworkExplosion.Shape.SMALL_BALL, IntList.of(), IntList.of(), false, false);

    assertPreservesOrder(comparator, Lists.newArrayList(
        createRocket(1, List.of(explosion)),
        createRocket(1, List.of(explosion, explosion)),
        createRocket(1, List.of(explosion, explosion, explosion))
    ));
  }

  private static ItemStack createRocket(int duration) {
    return createRocket(duration, List.of());
  }

  private static ItemStack createRocket(int duration, List<FireworkExplosion> explosions) {
    ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
    stack.set(DataComponents.FIREWORKS, new Fireworks(duration, explosions));
    return stack;
  }
}
