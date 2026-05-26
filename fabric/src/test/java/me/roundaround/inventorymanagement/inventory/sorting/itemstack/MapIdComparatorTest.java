package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapIdComparatorTest extends BaseMinecraftTest {
  private static MapIdComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new MapIdComparator();
  }

  @Test
  void ignoresNonMapItems() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void sortsByMapIdAscending() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createMap(1),
        createMap(5),
        createMap(10),
        createMap(100)
    ));
  }

  @Test
  void sameMapIdsAreEqual() {
    assertEquals(0, comparator.compare(createMap(42), createMap(42)));
  }

  private static ItemStack createMap(int id) {
    ItemStack stack = new ItemStack(Items.FILLED_MAP);
    stack.set(DataComponents.MAP_ID, new MapId(id));
    return stack;
  }
}
