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
 * HOT-02: a second hotbar swap restores the previously-raised row before raising the next.
 * {@code swapHotbarRows(prev, new)} first un-swaps {@code prev} (returning it home) then swaps {@code new}
 * into the hotbar. Seeds markers in hotbar (H), row 1 (A), and row 2 (B); fires {@code sendHotbarSwap(0, 1)}
 * then {@code sendHotbarSwap(1, 2)}; asserts the final layout is hotbar=B, row 1=A (back home), row 2=H.
 */
@ClientGameTest
public class HotbarSwapSequentialRestoresTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // hotbar (0-8) = H markers, row 1 (9-17) = A markers, row 2 (18-26) = B markers.
      for (int i = 0; i < 9; i++) {
        world.setInventoryItem(i, InvGameTests.named(Items.STONE, 1, "H-" + i));
        world.setInventoryItem(9 + i, InvGameTests.named(Items.DIRT, 1, "A-" + i));
        world.setInventoryItem(18 + i, InvGameTests.named(Items.OAK_PLANKS, 1, "B-" + i));
      }
      context.waitTicks(2);

      // Raise row 1 into the hotbar: hotbar=A, row1=H, row2=B.
      InvGameTests.act(context, mc -> ClientNetworking.sendHotbarSwap(0, 1));
      // Restore row 1, then raise row 2: hotbar=B, row1=A (home), row2=H.
      InvGameTests.act(context, mc -> ClientNetworking.sendHotbarSwap(1, 2));

      for (int i = 0; i < 9; i++) {
        ItemStack hotbar = world.getInventoryItem(i);
        ItemStack row1 = world.getInventoryItem(9 + i);
        ItemStack row2 = world.getInventoryItem(18 + i);

        // Hotbar now holds row 2's original B markers.
        assertItem(hotbar, Items.OAK_PLANKS, 1);
        assertSameComponents(hotbar, InvGameTests.named(Items.OAK_PLANKS, 1, "B-" + i));

        // Row 1 is back to its original A markers.
        assertItem(row1, Items.DIRT, 1);
        assertSameComponents(row1, InvGameTests.named(Items.DIRT, 1, "A-" + i));

        // Row 2 now holds the original hotbar H markers.
        assertItem(row2, Items.STONE, 1);
        assertSameComponents(row2, InvGameTests.named(Items.STONE, 1, "H-" + i));
      }
    }
  }
}
