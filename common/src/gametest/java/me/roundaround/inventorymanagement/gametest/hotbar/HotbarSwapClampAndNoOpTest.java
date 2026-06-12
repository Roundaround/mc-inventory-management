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
 * HOT-04: the row-swap clamps out-of-range rows and no-ops when {@code prev == new}. Two phases in one
 * class (inventory re-seeded between them):
 * <ul>
 *   <li>Phase A: {@code sendHotbarSwap(2, 2)} — equal rows short-circuit, so the entire inventory is
 *       unchanged.</li>
 *   <li>Phase B: {@code sendHotbarSwap(0, 9)} — the new row clamps to 3, so row 3 (slots 27-35) swaps
 *       with the hotbar.</li>
 * </ul>
 */
@ClientGameTest
public class HotbarSwapClampAndNoOpTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // ---- Phase A: prev == new is a no-op. ----
      seedFullInventory(world);
      context.waitTicks(2);

      // inventorySnapshot() compares slots 0-35 by value; nothing should move.
      context.assertUnchanged(world::inventorySnapshot, () -> {
        context.runOnClient(mc -> ClientNetworking.sendHotbarSwap(2, 2));
        context.waitTicks(InvGameTests.SETTLE_TICKS);
      });

      // ---- Phase B: new row 9 clamps to row 3; row 3 (27-35) swaps with the hotbar. ----
      seedFullInventory(world);
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendHotbarSwap(0, 9));

      for (int i = 0; i < 9; i++) {
        ItemStack hotbar = world.getInventoryItem(i);
        ItemStack row3 = world.getInventoryItem(27 + i);

        // Hotbar now holds the old row-3 markers.
        assertItem(hotbar, Items.GOLD_INGOT, 1);
        assertSameComponents(hotbar, InvGameTests.named(Items.GOLD_INGOT, 1, "row3-" + i));

        // Row 3 now holds the old hotbar markers.
        assertItem(row3, Items.STONE, 1);
        assertSameComponents(row3, InvGameTests.named(Items.STONE, 1, "hotbar-" + i));
      }

      // Rows 1 and 2 were never touched by a (0 -> clamped-3) swap.
      for (int i = 0; i < 9; i++) {
        ItemStack row1 = world.getInventoryItem(9 + i);
        ItemStack row2 = world.getInventoryItem(18 + i);
        assertItem(row1, Items.DIRT, 1);
        assertSameComponents(row1, InvGameTests.named(Items.DIRT, 1, "row1-" + i));
        assertItem(row2, Items.OAK_PLANKS, 1);
        assertSameComponents(row2, InvGameTests.named(Items.OAK_PLANKS, 1, "row2-" + i));
      }
    }
  }

  /** Seed all four rows (hotbar 0-8, rows 1/2/3 at 9-17/18-26/27-35) with distinct per-slot markers. */
  private static void seedFullInventory(ClientWorld world) {
    for (int i = 0; i < 9; i++) {
      world.setInventoryItem(i, InvGameTests.named(Items.STONE, 1, "hotbar-" + i));
      world.setInventoryItem(9 + i, InvGameTests.named(Items.DIRT, 1, "row1-" + i));
      world.setInventoryItem(18 + i, InvGameTests.named(Items.OAK_PLANKS, 1, "row2-" + i));
      world.setInventoryItem(27 + i, InvGameTests.named(Items.GOLD_INGOT, 1, "row3-" + i));
    }
  }
}
