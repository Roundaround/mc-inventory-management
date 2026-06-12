package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertEmpty;
import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * SORT-02: sorting consolidates partial stacks of the same item. Seeds a chest with two 40-count
 * dirt stacks and a lone stone, then asserts the sort reorders alphabetically (dirt before stone)
 * AND merges the dirt partials, yielding {@code [dirt x64, dirt x16, stone x1]} in slots 0..2.
 */
@ClientGameTest
public class SortMergesPartialStacksTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 40));
      world.setContainerItem(chest.pos(), 5, new ItemStack(Items.DIRT, 40));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.STONE, 1));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> after = world.containerSnapshot(chest.pos());
      assertSlot(after, 0, Items.DIRT, 64);
      assertSlot(after, 1, Items.DIRT, 16);
      assertSlot(after, 2, Items.STONE, 1);
      // Everything compacted to the front; the previously-seeded slot 5 is now empty.
      assertEmpty(after, 5);
    }
  }
}
