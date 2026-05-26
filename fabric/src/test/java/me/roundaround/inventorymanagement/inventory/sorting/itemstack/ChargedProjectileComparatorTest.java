package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChargedProjectileComparatorTest extends BaseMinecraftTest {
  private static ChargedProjectileComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new ChargedProjectileComparator();
  }

  @Test
  void ignoresNonCrossbowItems() {
    ItemStack a = new ItemStack(Items.DIAMOND);
    ItemStack b = new ItemStack(Items.DIRT);
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void loadedSortsBeforeUnloaded() {
    ItemStack loaded = createCharged(Items.ARROW);
    ItemStack unloaded = createEmpty();
    assertTrue(comparator.compare(loaded, unloaded) < 0);
  }

  @Test
  void bothUnloadedAreEqual() {
    assertEquals(0, comparator.compare(createEmpty(), createEmpty()));
  }

  private static ItemStack createCharged(net.minecraft.world.item.Item projectile) {
    ItemStack stack = new ItemStack(Items.CROSSBOW);
    stack.set(DataComponents.CHARGED_PROJECTILES,
        new ChargedProjectiles(List.of(new ItemStackTemplate(projectile))));
    return stack;
  }

  private static ItemStack createEmpty() {
    ItemStack stack = new ItemStack(Items.CROSSBOW);
    stack.set(DataComponents.CHARGED_PROJECTILES, new ChargedProjectiles(List.of()));
    return stack;
  }
}
