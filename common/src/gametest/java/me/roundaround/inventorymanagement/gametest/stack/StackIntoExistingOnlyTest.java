package me.roundaround.inventorymanagement.gametest.stack;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertContains;
import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * STACK-01: auto-stack (player&rarr;container) only grows EXISTING matching stacks in the destination and
 * never fills empty slots. Chest holds dirt x10 (slot 0); player has dirt x20 (slot 9) and stone x5
 * (slot 10). After {@code sendStack(true)} the chest dirt is all merged into its single existing stack
 * (slot 0 = 30), no dirt leaks into other chest slots, the player loses all dirt, and stone is untouched
 * (no matching stone stack exists in the chest, so it stays put).
 */
@ClientGameTest
public class StackIntoExistingOnlyTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 10));
      world.setInventoryItem(9, new ItemStack(Items.DIRT, 20));
      world.setInventoryItem(10, new ItemStack(Items.STONE, 5));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendStack(true));

      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      // All dirt merged into the single pre-existing destination stack at slot 0.
      assertSlot(chestAfter, 0, Items.DIRT, 30);
      assertContains(chestAfter, Items.DIRT, 30);
      // Auto-stack must never fill empty slots, so dirt lives only in slot 0.
      for (int i = 1; i < chestAfter.size(); i++) {
        if (chestAfter.get(i).getItem() == Items.DIRT) {
          throw new GameTestAssertionException(
              "dirt leaked into empty chest slot " + i + "; auto-stack must only grow existing stacks");
        }
      }

      List<ItemStack> playerAfter = world.inventorySnapshot();
      // Player gave up all dirt but keeps the stone (no matching chest stack to grow).
      assertContains(playerAfter, Items.DIRT, 0);
      assertSlot(playerAfter, 10, Items.STONE, 5);
      assertContains(playerAfter, Items.STONE, 5);
    }
  }
}
