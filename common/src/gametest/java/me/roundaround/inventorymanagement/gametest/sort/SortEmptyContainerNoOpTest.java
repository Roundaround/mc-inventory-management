package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertEmpty;

/**
 * SORT-05: sorting an empty container is a harmless no-op. Opens a freshly-placed (empty) chest,
 * fires a container sort, and asserts every slot is still empty and no exception was thrown.
 */
@ClientGameTest
public class SortEmptyContainerNoOpTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> after = world.containerSnapshot(chest.pos());
      for (int i = 0; i < after.size(); i++) {
        assertEmpty(after, i);
      }
    }
  }
}
