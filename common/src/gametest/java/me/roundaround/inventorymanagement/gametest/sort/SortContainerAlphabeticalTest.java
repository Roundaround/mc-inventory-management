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

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * SORT-01: sorting a container reorders its contents alphabetically (default {@code sortMode}) and
 * compacts them to the front. Seeds {@code [stone, apple, dirt]}, sorts via the real client&rarr;server
 * round trip, and asserts {@code [apple, dirt, stone]} in slots 0..2.
 */
@ClientGameTest
public class SortContainerAlphabeticalTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> after = world.containerSnapshot(chest.pos());
      assertSlot(after, 0, Items.APPLE, 1);
      assertSlot(after, 1, Items.DIRT, 1);
      assertSlot(after, 2, Items.STONE, 1);
    }
  }
}
