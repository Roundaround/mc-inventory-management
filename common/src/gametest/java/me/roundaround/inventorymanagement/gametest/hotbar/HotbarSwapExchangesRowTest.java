package me.roundaround.inventorymanagement.gametest.hotbar;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static me.roundaround.trove.gametest.GameTestAssertions.assertItem;
import static me.roundaround.trove.gametest.GameTestAssertions.assertSameComponents;

/**
 * HOT-01: a hotbar-row swap exchanges all nine slots of the chosen main-grid row with the hotbar.
 * Seeds distinct named markers in the hotbar (0-8) and in row 1 (9-17), fires
 * {@code sendHotbarSwap(0, 1)}, and asserts every one of the nine pairs swapped — the hotbar now
 * holds the old row-1 markers and row 1 holds the old hotbar markers. Needs no open container.
 */
@ClientGameTest
public class HotbarSwapExchangesRowTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // Seed the hotbar (slots 0-8) and row 1 (slots 9-17) with distinct, individually tracked markers.
      for (int i = 0; i < 9; i++) {
        world.setInventoryItem(i, InvGameTests.named(Items.STONE, 1, "hotbar-" + i));
        world.setInventoryItem(9 + i, InvGameTests.named(Items.DIRT, 1, "row1-" + i));
      }
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendHotbarSwap(0, 1));

      for (int i = 0; i < 9; i++) {
        ItemStack hotbar = world.getInventoryItem(i);
        ItemStack row1 = world.getInventoryItem(9 + i);

        // The hotbar slot should now carry the marker that started in the matching row-1 slot.
        assertItem(hotbar, Items.DIRT, 1);
        assertSameComponents(hotbar, InvGameTests.named(Items.DIRT, 1, "row1-" + i));

        // The row-1 slot should now carry the marker that started in the matching hotbar slot.
        assertItem(row1, Items.STONE, 1);
        assertSameComponents(row1, InvGameTests.named(Items.STONE, 1, "hotbar-" + i));
      }
    }
  }
}
