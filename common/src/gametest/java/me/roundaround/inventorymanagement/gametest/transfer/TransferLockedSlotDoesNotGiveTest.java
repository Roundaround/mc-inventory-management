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
 * XFER-04a: a locked player slot is excluded as a SOURCE for transfer-all. Slot 27 is locked and holds a
 * named diamond marker; after {@code sendTransfer(true)} (player &rarr; container) the marker stays put — the
 * lock keeps the slot from giving up its item.
 */
@ClientGameTest
public class TransferLockedSlotDoesNotGiveTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      InvGameTests.lockSlots(context, 27);
      ItemStack marker = InvGameTests.named(Items.DIAMOND, 3, "locked-marker");
      world.setInventoryItem(27, marker);
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(true));

      ItemStack after = world.getInventoryItem(27);
      if (after.getItem() != Items.DIAMOND || after.getCount() != 3) {
        throw new GameTestAssertionException(
            "locked slot 27 should not give up its marker on transfer, but held "
                + (after.isEmpty() ? "<empty>" : after.getCount() + "x " + after.getItem()));
      }

      // The marker did not leak into the chest.
      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      int inChest = 0;
      for (ItemStack stack : chestAfter) {
        if (stack.getItem() == Items.DIAMOND) {
          inChest += stack.getCount();
        }
      }
      if (inChest != 0) {
        throw new GameTestAssertionException("locked marker leaked into the chest: found " + inChest + " diamonds");
      }
    }
  }
}
