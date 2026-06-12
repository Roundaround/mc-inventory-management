package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * SORT-09: {@code sendSortAll} sorts the player main grid AND the open container in one shot. Phase 1
 * scrambles both and asserts both come back alphabetical. Phase 2 covers the empty-container case:
 * with the open chest emptied, the all-empty container sort is a no-op while the player grid still
 * sorts. (The client's true {@code containerSorted.isEmpty()} player-only fallback only fires with no
 * container open at all; an empty-but-open chest still rides the {@code SortAllC2S} path, whose
 * container half is harmlessly inert here — either way the player sort applies, which is what we
 * assert.)
 */
@ClientGameTest
public class SortAllTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      // --- Phase 1: both sides populated and scrambled ---
      seedPlayer(world);
      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSortAll(mc.player));

      List<ItemStack> player = world.inventorySnapshot();
      assertSlot(player, 9, Items.APPLE, 1);
      assertSlot(player, 10, Items.DIRT, 1);
      assertSlot(player, 11, Items.STONE, 1);

      List<ItemStack> container = world.containerSnapshot(chest.pos());
      assertSlot(container, 0, Items.APPLE, 1);
      assertSlot(container, 1, Items.DIRT, 1);
      assertSlot(container, 2, Items.STONE, 1);

      // --- Phase 2: empty-container fallback still sorts the player ---
      clearChest(world, chest.pos());
      seedPlayer(world);
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSortAll(mc.player));

      List<ItemStack> playerAgain = world.inventorySnapshot();
      assertSlot(playerAgain, 9, Items.APPLE, 1);
      assertSlot(playerAgain, 10, Items.DIRT, 1);
      assertSlot(playerAgain, 11, Items.STONE, 1);
    }
  }

  /** Scramble the player main grid (slots 9-35) so a sort must reorder it to [apple, dirt, stone]. */
  private static void seedPlayer(ClientWorld world) {
    world.setInventoryItem(9, new ItemStack(Items.STONE));
    world.setInventoryItem(10, new ItemStack(Items.APPLE));
    world.setInventoryItem(11, new ItemStack(Items.DIRT));
  }

  private static void clearChest(ClientWorld world, BlockPos pos) {
    world.setContainerItem(pos, 0, ItemStack.EMPTY);
    world.setContainerItem(pos, 1, ItemStack.EMPTY);
    world.setContainerItem(pos, 2, ItemStack.EMPTY);
    world.context().waitTicks(2);
  }
}
