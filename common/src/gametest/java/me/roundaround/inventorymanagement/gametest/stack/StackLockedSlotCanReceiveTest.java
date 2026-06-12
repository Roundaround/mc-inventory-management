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

/**
 * STACK-04b: a locked player slot may still RECEIVE items when the player is the destination. Player slot
 * 18 (locked) = dirt x10; chest holds dirt x20. After {@code sendStack(false)} the chest is the source and
 * the locked slot grows past its original 10 — locking only blocks giving, not receiving.
 */
@ClientGameTest
public class StackLockedSlotCanReceiveTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      InvGameTests.lockSlots(context, 18);
      world.setInventoryItem(18, new ItemStack(Items.DIRT, 10));
      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 20));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendStack(false));

      List<ItemStack> playerAfter = world.inventorySnapshot();
      int locked = playerAfter.get(18).getItem() == Items.DIRT ? playerAfter.get(18).getCount() : 0;
      if (locked <= 10) {
        throw new GameTestAssertionException(
            "locked slot 18 should still receive items when the player is the destination, but held " + locked
                + " dirt (started at 10)");
      }
      // Conservation sanity: the 30 total dirt is preserved across both inventories.
      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      int total = countDirt(playerAfter) + countDirt(chestAfter);
      if (total != 30) {
        throw new GameTestAssertionException("expected 30 dirt total after stack, found " + total);
      }
      // The destination stack actually grew, so the chest gave up at least some dirt.
      assertContains(chestAfter, Items.DIRT, 30 - locked);
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
