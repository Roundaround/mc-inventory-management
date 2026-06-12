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
 * STACK-05: auto-stack uses {@code ItemStack.isSameItemSameComponents}, so two same-item stacks with
 * different {@code CUSTOM_NAME} markers do NOT merge. Chest slot 0 = named(STONE,10,"A"); player slot 9 =
 * named(STONE,10,"B"). After {@code sendStack(true)} the differing component blocks the merge: the chest
 * "A" stack stays at 10 and the player keeps its "B" stack at 10.
 */
@ClientGameTest
public class StackComponentMismatchTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, InvGameTests.named(Items.STONE, 10, "A"));
      world.setInventoryItem(9, InvGameTests.named(Items.STONE, 10, "B"));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendStack(true));

      // The chest "A" stack is unchanged; the player "B" stack could not merge into it.
      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      assertSlot(chestAfter, 0, Items.STONE, 10);
      assertContains(chestAfter, Items.STONE, 10);

      List<ItemStack> playerAfter = world.inventorySnapshot();
      assertSlot(playerAfter, 9, Items.STONE, 10);
      assertContains(playerAfter, Items.STONE, 10);
    }
  }
}
