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
 * XFER-03: transfer-all (container &rarr; player) empties a stocked chest into the player's main grid. The
 * chest holds a spread of mixed items (dirt, stone, an apple stack, a diamond sword) and the player main
 * grid starts empty. After {@code sendTransfer(false)} the items land in the player inventory and the
 * chest is cleared.
 */
@ClientGameTest
public class TransferFromContainerTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 32));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.STONE, 16));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.APPLE, 8));
      world.setContainerItem(chest.pos(), 3, new ItemStack(Items.DIAMOND_SWORD));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(false));

      List<ItemStack> playerAfter = world.inventorySnapshot();
      assertContains(playerAfter, Items.DIRT, 32);
      assertContains(playerAfter, Items.STONE, 16);
      assertContains(playerAfter, Items.APPLE, 8);
      assertContains(playerAfter, Items.DIAMOND_SWORD, 1);

      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      assertContains(chestAfter, Items.DIRT, 0);
      assertContains(chestAfter, Items.STONE, 0);
      assertContains(chestAfter, Items.APPLE, 0);
      assertContains(chestAfter, Items.DIAMOND_SWORD, 0);
    }
  }
}
