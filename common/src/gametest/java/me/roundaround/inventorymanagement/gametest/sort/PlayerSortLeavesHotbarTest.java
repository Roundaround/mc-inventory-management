package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * SORT-03: a player-inventory sort reorders the main grid (slots 9-35) alphabetically while leaving
 * the hotbar (slots 0-8) completely untouched. Seeds the hotbar with named markers and scrambles the
 * main grid, sorts the player side, then asserts each hotbar marker is byte-for-byte unchanged and
 * the grid is now alphabetically ordered.
 */
@ClientGameTest
public class PlayerSortLeavesHotbarTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // Open a chest so a container-side context exists, but we sort the PLAYER side.
      InvGameTests.openChest(context, world);

      // Hotbar markers (slots 0-8): distinctly-named so we can verify they never move.
      for (int slot = 0; slot < 9; slot++) {
        world.setInventoryItem(slot, InvGameTests.named(Items.STICK, 1, "hotbar-" + slot));
      }

      // Scramble the main grid (slots 9-35): out-of-order so sorting must reorder it.
      world.setInventoryItem(9, new ItemStack(Items.STONE));
      world.setInventoryItem(10, new ItemStack(Items.APPLE));
      world.setInventoryItem(11, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, true));

      // Hotbar markers unchanged: same item, count, and CUSTOM_NAME component.
      for (int slot = 0; slot < 9; slot++) {
        ItemStack stack = world.getInventoryItem(slot);
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        String expected = "hotbar-" + slot;
        if (stack.getItem() != Items.STICK || stack.getCount() != 1
            || name == null || !name.getString().equals(expected)) {
          throw new GameTestAssertionException(
              "hotbar slot " + slot + " should still hold marker '" + expected + "' but was "
                  + stack.getCount() + "x " + stack.getItem() + " name=" + (name == null ? "<none>" : name.getString()));
        }
      }

      // Main grid sorted alphabetically: Apple, Dirt, Stone.
      List<ItemStack> after = world.inventorySnapshot();
      assertSlot(after, 9, Items.APPLE, 1);
      assertSlot(after, 10, Items.DIRT, 1);
      assertSlot(after, 11, Items.STONE, 1);
    }
  }
}
