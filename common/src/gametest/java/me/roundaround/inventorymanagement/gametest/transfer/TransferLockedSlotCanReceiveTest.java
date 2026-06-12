package me.roundaround.inventorymanagement.gametest.transfer;

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

/**
 * XFER-04b: a locked player slot may still RECEIVE during transfer-all when the player is the destination.
 * Slot 27 is locked and pre-seeded with dirt x10; slots 9..26 are packed with full cobblestone so dirt
 * cannot land there, leaving the locked slot 27 as the merge target. After {@code sendTransfer(false)}
 * (container &rarr; player) the chest's dirt grows the locked stack past its original 10 — locking only blocks
 * giving, not receiving.
 */
@ClientGameTest
public class TransferLockedSlotCanReceiveTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      InvGameTests.lockSlots(context, 27);

      // Pack every earlier main-grid slot (9..26) with full cobblestone so incoming dirt cannot place or
      // merge there; the locked slot 27 (seeded with a partial dirt stack) is the only viable destination.
      for (int slot = 9; slot < 27; slot++) {
        world.setInventoryItem(slot, new ItemStack(Items.COBBLESTONE, 64));
      }
      world.setInventoryItem(27, new ItemStack(Items.DIRT, 10));
      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 20));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(false));

      List<ItemStack> playerAfter = world.inventorySnapshot();
      ItemStack locked = playerAfter.get(27);
      int lockedCount = locked.getItem() == Items.DIRT ? locked.getCount() : 0;
      if (lockedCount <= 10) {
        throw new GameTestAssertionException(
            "locked slot 27 should still receive items when the player is the destination, but held "
                + lockedCount + " dirt (started at 10)");
      }

      // Conservation: 30 dirt total spread across player + chest.
      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      int total = countDirt(playerAfter) + countDirt(chestAfter);
      if (total != 30) {
        throw new GameTestAssertionException("expected 30 dirt total after transfer, found " + total);
      }
    }
  }

  private static int countDirt(List<ItemStack> inventory) {
    int count = 0;
    for (ItemStack stack : inventory) {
      if (stack.getItem() == Items.DIRT) {
        count += stack.getCount();
      }
    }
    return count;
  }
}
