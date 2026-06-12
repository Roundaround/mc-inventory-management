package me.roundaround.inventorymanagement.gametest.security;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.inventorymanagement.network.Networking;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertConserved;

/**
 * SEC-02: a forged container {@code SortC2S} that names the same source slot twice is rejected as a
 * full no-op, so it cannot duplicate an item. {@code ServerInventoryHelper.applySort} removes each
 * referenced source slot from the occupied-slot set exactly once; the second reference to an
 * already-removed slot fails the {@code slotsWithItems.remove(...)} check and bails before any mutation.
 * Here the chest holds 3 items in slots 0,1,2 but the forged list references slot 0 twice (and omits
 * slot 2), which must leave the chest exactly as seeded — verified by conserving the before/after
 * multiset so no item is created or lost.
 */
@ClientGameTest
public class ForgedPermutationDuplicateIndexNoOpTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      List<ItemStack> before = world.containerSnapshot(chest.pos());

      // Occupied source slots are {0, 1, 2}. This forged permutation names source slot 0 twice — which,
      // if honored, would duplicate the stone — and omits occupied slot 2. The server must reject it.
      List<Integer> forged = List.of(0, 0, 1);

      InvGameTests.act(context, mc -> TroveNetworking.sendToServer(new Networking.SortC2S(false, forged, 0L)));

      List<ItemStack> after = world.containerSnapshot(chest.pos());
      assertConserved(before, after);
    }
  }
}
