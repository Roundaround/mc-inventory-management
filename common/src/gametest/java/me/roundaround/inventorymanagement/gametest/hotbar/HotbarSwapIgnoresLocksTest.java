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
 * HOT-03: the hotbar-row swap bypasses the locked-slots feature. Slot 9 (first slot of row 1) is locked
 * via the config, then the same distinct-marker layout as HOT-01 is seeded and {@code sendHotbarSwap(0, 1)}
 * fired. Asserts the locked slot's item still exchanged with the hotbar exactly like the unlocked slots —
 * the swap is a display convenience and ignores locks (see {@code swapHotbarRows}'s contract).
 */
@ClientGameTest
public class HotbarSwapIgnoresLocksTest implements ClientTest {
  private static final int LOCKED_ROW1_SLOT = 9;

  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // Lock the first slot of row 1; auto-unlocks on cleanup.
      InvGameTests.lockSlots(context, LOCKED_ROW1_SLOT);

      for (int i = 0; i < 9; i++) {
        world.setInventoryItem(i, InvGameTests.named(Items.STONE, 1, "hotbar-" + i));
        world.setInventoryItem(9 + i, InvGameTests.named(Items.DIRT, 1, "row1-" + i));
      }
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendHotbarSwap(0, 1));

      // The locked row-1 slot (index 9, i == 0) must have swapped despite the lock: its old DIRT marker
      // is now in hotbar slot 0, and the old hotbar STONE marker is now in the locked slot.
      ItemStack lockedAfter = world.getInventoryItem(LOCKED_ROW1_SLOT);
      assertItem(lockedAfter, Items.STONE, 1);
      assertSameComponents(lockedAfter, InvGameTests.named(Items.STONE, 1, "hotbar-0"));

      ItemStack hotbar0After = world.getInventoryItem(0);
      assertItem(hotbar0After, Items.DIRT, 1);
      assertSameComponents(hotbar0After, InvGameTests.named(Items.DIRT, 1, "row1-0"));

      // For good measure, confirm the rest of the row swapped too (locks never gated any slot).
      for (int i = 1; i < 9; i++) {
        ItemStack hotbar = world.getInventoryItem(i);
        ItemStack row1 = world.getInventoryItem(9 + i);
        assertItem(hotbar, Items.DIRT, 1);
        assertSameComponents(hotbar, InvGameTests.named(Items.DIRT, 1, "row1-" + i));
        assertItem(row1, Items.STONE, 1);
        assertSameComponents(row1, InvGameTests.named(Items.STONE, 1, "hotbar-" + i));
      }
    }
  }
}
