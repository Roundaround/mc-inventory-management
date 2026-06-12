package me.roundaround.inventorymanagement.gametest.transfer;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertContains;

/**
 * XFER-01: transfer-all (player &rarr; container) fills empty container slots with every item type from the
 * player main grid. Seeds slot 9 = dirt x20, slot 10 = stone x5, slot 11 = a diamond sword into an empty
 * chest, fires {@code sendTransfer(true)}, and asserts all three types now live in the chest and the
 * player main grid is empty.
 */
@ClientGameTest
public class TransferFillsAndMergesTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setInventoryItem(9, new ItemStack(Items.DIRT, 20));
      world.setInventoryItem(10, new ItemStack(Items.STONE, 5));
      world.setInventoryItem(11, new ItemStack(Items.DIAMOND_SWORD));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(true));

      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      assertContains(chestAfter, Items.DIRT, 20);
      assertContains(chestAfter, Items.STONE, 5);
      assertContains(chestAfter, Items.DIAMOND_SWORD, 1);

      List<ItemStack> playerAfter = world.inventorySnapshot();
      assertContains(playerAfter, Items.DIRT, 0);
      assertContains(playerAfter, Items.STONE, 0);
      assertContains(playerAfter, Items.DIAMOND_SWORD, 0);
    }
  }
}
