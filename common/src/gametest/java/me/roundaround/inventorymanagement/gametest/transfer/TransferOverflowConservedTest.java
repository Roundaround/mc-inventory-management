package me.roundaround.inventorymanagement.gametest.transfer;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertConserved;
import static me.roundaround.trove.gametest.GameTestAssertions.assertContains;

/**
 * XFER-02: transfer-all stops when the container runs out of room and never duplicates or loses items.
 * A 27-slot chest is pre-filled with cobblestone in slots 0..25, leaving exactly slot 26 free; the player
 * carries dirt x128 (two x64 stacks). After {@code sendTransfer(true)} the chest gains one dirt x64 (the
 * single free slot) and the player keeps the other dirt x64. The combined (player main + container)
 * multiset is asserted unchanged via {@link me.roundaround.trove.gametest.GameTestAssertions#assertConserved}.
 */
@ClientGameTest
public class TransferOverflowConservedTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      // Fill every chest slot but the last with cobblestone (a different item so dirt cannot merge in),
      // leaving exactly slot 26 free.
      for (int i = 0; i < 26; i++) {
        world.setContainerItem(chest.pos(), i, new ItemStack(Items.COBBLESTONE, 64));
      }
      world.setInventoryItem(9, new ItemStack(Items.DIRT, 64));
      world.setInventoryItem(10, new ItemStack(Items.DIRT, 64));
      context.waitTicks(2);

      List<ItemStack> before = combined(world, chest.pos());

      InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(true));

      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      List<ItemStack> playerAfter = world.inventorySnapshot();

      // The chest absorbed exactly one stack of dirt; the player keeps the other.
      assertContains(chestAfter, Items.DIRT, 64);
      assertContains(playerAfter, Items.DIRT, 64);

      // Nothing duplicated or vanished across the move.
      List<ItemStack> after = new ArrayList<>(chestAfter);
      after.addAll(playerAfter);
      assertConserved(before, after);
    }
  }

  private static List<ItemStack> combined(ClientWorld world, net.minecraft.core.BlockPos pos) {
    List<ItemStack> out = new ArrayList<>(world.containerSnapshot(pos));
    out.addAll(world.inventorySnapshot());
    return out;
  }
}
