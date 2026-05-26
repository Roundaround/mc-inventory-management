package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerHeadNameComparatorTest extends BaseMinecraftTest {
  private static PlayerHeadNameComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new PlayerHeadNameComparator();
  }

  @Test
  void ignoresNonHeadItems() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void sortsAlphabeticallyByPlayerName() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createHead("Alice"),
        createHead("Bob"),
        createHead("Charlie")
    ));
  }

  @Test
  void namedHeadsSortBeforeUnnamed() {
    PlayerHeadNameComparator comp = new PlayerHeadNameComparator();
    ItemStack named = createHead("Alice");
    ItemStack unnamed = new ItemStack(Items.PLAYER_HEAD);

    assertTrue(comp.compare(named, unnamed) < 0, "Named heads should sort before unnamed (nullsLast)");
    assertTrue(comp.compare(unnamed, named) > 0, "Unnamed heads should sort after named (nullsLast)");
  }

  private static ItemStack createHead(String playerName) {
    ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
    stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(playerName));
    return stack;
  }
}
