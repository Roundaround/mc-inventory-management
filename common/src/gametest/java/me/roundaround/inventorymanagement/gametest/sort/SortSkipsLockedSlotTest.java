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

/**
 * SORT-04: a locked player slot is excluded from the sort range, so its contents stay exactly where
 * they are. Locks main-grid slot 20, drops a named marker there, scrambles surrounding slots, sorts
 * the player side, and asserts slot 20 still holds the very same marker (item, count, and name).
 */
@ClientGameTest
public class SortSkipsLockedSlotTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openChest(context, world);

      // Lock slot 20 (auto-unlocked on cleanup).
      InvGameTests.lockSlots(context, 20);

      // Marker that must remain pinned in slot 20.
      world.setInventoryItem(20, InvGameTests.named(Items.DIAMOND, 1, "pinned"));

      // Scramble surrounding main-grid slots so a sort would normally rearrange everything.
      world.setInventoryItem(15, new ItemStack(Items.STONE));
      world.setInventoryItem(16, new ItemStack(Items.APPLE));
      world.setInventoryItem(25, new ItemStack(Items.DIRT));
      world.setInventoryItem(30, new ItemStack(Items.STONE));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, true));

      ItemStack pinned = world.getInventoryItem(20);
      Component name = pinned.get(DataComponents.CUSTOM_NAME);
      if (pinned.getItem() != Items.DIAMOND || pinned.getCount() != 1
          || name == null || !name.getString().equals("pinned")) {
        throw new GameTestAssertionException(
            "locked slot 20 should still hold the pinned marker but was "
                + pinned.getCount() + "x " + pinned.getItem()
                + " name=" + (name == null ? "<none>" : name.getString()));
      }
    }
  }
}
