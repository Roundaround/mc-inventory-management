package me.roundaround.inventorymanagement.gametest.stack;

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
import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * STACK-02: auto-stack respects the destination stack's max size. Chest slot 0 = dirt x60, player has
 * dirt x20 (slot 9). After {@code sendStack(true)} the chest stack fills only to 64 (the only existing
 * destination stack), and the leftover 16 dirt stays with the player because auto-stack never spills
 * into empty chest slots.
 */
@ClientGameTest
public class StackRespectsMaxStackTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 60));
      world.setInventoryItem(9, new ItemStack(Items.DIRT, 20));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendStack(true));

      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      assertSlot(chestAfter, 0, Items.DIRT, 64);
      // Max stack honored: nothing spilled into other chest slots.
      assertContains(chestAfter, Items.DIRT, 64);

      List<ItemStack> playerAfter = world.inventorySnapshot();
      assertSlot(playerAfter, 9, Items.DIRT, 16);
      assertContains(playerAfter, Items.DIRT, 16);
    }
  }
}
