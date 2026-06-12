package me.roundaround.inventorymanagement.gametest.stack;

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

/**
 * STACK-06: auto-stack with no matching destination stack is a no-op — it can only grow EXISTING matching
 * stacks, never create new ones. Chest holds stone only; player holds dirt only. {@code sendStack(true)}
 * finds no stone stack in the player and no dirt stack in the chest to grow, so nothing moves. Asserted
 * with {@code assertUnchanged} over a combined chest+player snapshot ({@code List<ItemStack>} compares by
 * value), which also covers conservation.
 */
@ClientGameTest
public class StackNoMatchingDestTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE, 10));
      world.setInventoryItem(9, new ItemStack(Items.DIRT, 10));
      context.waitTicks(2);

      context.assertUnchanged(
          () -> combinedSnapshot(world, chest.pos()),
          () -> InvGameTests.act(context, mc -> ClientNetworking.sendStack(true)));
    }
  }

  /** Container slots followed by player main-inventory slots, as one value-comparable list. */
  private static List<ItemStack> combinedSnapshot(ClientWorld world, net.minecraft.core.BlockPos pos) {
    List<ItemStack> combined = new ArrayList<>();
    combined.addAll(world.containerSnapshot(pos));
    combined.addAll(world.inventorySnapshot());
    return combined;
  }
}
