package me.roundaround.inventorymanagement.gametest.transfer;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * XFER-06: transfer-all honors per-slot placement restrictions. A shulker box container's slots reject
 * shulker box items ({@code ShulkerBoxSlot.mayPlace -> BlockItem.canFitInsideContainerItems}, which is
 * false for shulker boxes, since boxes cannot nest). The player holds a shulker box item (slot 9) plus a
 * plain dirt stack (slot 10); after {@code sendTransfer(true)} the dirt lands in the box (proving the op
 * ran) but the shulker box item stays with the player because the destination slots refuse it.
 */
@ClientGameTest
public class TransferRespectsSlotRestrictionTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // Open the shulker box with an empty hand first; only then seed the player inventory so the held
      // shulker box item never short-circuits the open by placing a block instead.
      InvGameTests.Opened box = InvGameTests.openContainer(context, world, Blocks.SHULKER_BOX, ShulkerBoxScreen.class);

      world.setInventoryItem(9, new ItemStack(Items.SHULKER_BOX));
      world.setInventoryItem(10, new ItemStack(Items.DIRT, 16));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(true));

      // The shulker box item could not be placed into the box and stays with the player.
      List<ItemStack> playerAfter = world.inventorySnapshot();
      int boxesWithPlayer = countItem(playerAfter, Items.SHULKER_BOX);
      if (boxesWithPlayer != 1) {
        throw new GameTestAssertionException(
            "shulker box must NOT transfer into a shulker box container, but the player now holds "
                + boxesWithPlayer + " (expected 1)");
      }

      List<ItemStack> boxAfter = world.containerSnapshot(box.pos());
      int boxesInBox = countItem(boxAfter, Items.SHULKER_BOX);
      if (boxesInBox != 0) {
        throw new GameTestAssertionException(
            "a shulker box was nested into the shulker box container (" + boxesInBox + " found); mayPlace=false should forbid it");
      }

      // Sanity: the transfer DID run — the plain dirt moved into the box.
      int dirtInBox = countItem(boxAfter, Items.DIRT);
      if (dirtInBox != 16) {
        throw new GameTestAssertionException(
            "expected the dirt (16) to transfer into the box to prove the op ran, but found " + dirtInBox);
      }
    }
  }

  private static int countItem(List<ItemStack> inventory, net.minecraft.world.item.Item item) {
    int count = 0;
    for (ItemStack stack : inventory) {
      if (stack.getItem() == item) {
        count += stack.getCount();
      }
    }
    return count;
  }
}
