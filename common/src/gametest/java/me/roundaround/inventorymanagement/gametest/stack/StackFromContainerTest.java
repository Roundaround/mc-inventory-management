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

/**
 * STACK-03: auto-stack from the container ({@code sendStack(false)}) grows the player's existing matching
 * stacks only. Player has dirt x10 (slot 9); chest holds dirt x20 + stone x5. After the op the player's
 * dirt total is 30 (merged into the existing player stack), the chest keeps its stone (no player stone
 * stack to receive it), and no stone appears in the player's empty slots.
 */
@ClientGameTest
public class StackFromContainerTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setInventoryItem(9, new ItemStack(Items.DIRT, 10));
      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.DIRT, 20));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.STONE, 5));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendStack(false));

      List<ItemStack> playerAfter = world.inventorySnapshot();
      assertContains(playerAfter, Items.DIRT, 30);
      // No matching player stone stack existed, so auto-stack adds none to the player.
      assertContains(playerAfter, Items.STONE, 0);

      List<ItemStack> chestAfter = world.containerSnapshot(chest.pos());
      assertContains(chestAfter, Items.STONE, 5);
      assertContains(chestAfter, Items.DIRT, 0);
    }
  }
}
