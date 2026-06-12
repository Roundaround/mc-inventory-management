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
 * STACK-04a: a locked player slot is excluded as a SOURCE. Player slot 18 (locked) = dirt x5; chest has a
 * matching dirt x10 stack. After {@code sendStack(true)} the locked slot must keep its dirt x5 untouched,
 * because the player inventory is the source and locked slots never give up items.
 */
@ClientGameTest
public class StackLockedSlotDoesNotGiveTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      InvGameTests.lockSlots(context, 18);
      world.setInventoryItem(18, new ItemStack(Items.DIRT, 5));
      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 10));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendStack(true));

      List<ItemStack> playerAfter = world.inventorySnapshot();
      // Locked source slot keeps every item.
      assertSlot(playerAfter, 18, Items.DIRT, 5);
      assertContains(playerAfter, Items.DIRT, 5);

      // The chest stack is unchanged because the only player dirt was locked.
      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      assertSlot(chestAfter, 0, Items.DIRT, 10);
    }
  }
}
