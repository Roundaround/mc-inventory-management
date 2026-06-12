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

/**
 * SEC-01: a forged container {@code SortC2S} whose permutation does not match the occupied-slot
 * multiset is rejected server-side as a full no-op. {@code ServerInventoryHelper.applySort} builds the
 * set of occupied source slots and, for every entry of the {@code sorted} list, must remove its
 * referenced source slot from that set; a reference to an empty slot (not in the set) bails before any
 * mutation, and an occupied slot left in the set at the end also bails. Here the chest holds 3 items in
 * slots 0,1,2 but the forged list references empty slot 5 and omits occupied slot 2, so the container
 * must be untouched.
 */
@ClientGameTest
public class ForgedPermutationWrongMultisetNoOpTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      // Occupied source slots are {0, 1, 2}. This forged permutation references empty slot 5 and never
      // names occupied slot 2 — a wrong multiset that the server must reject as a full no-op.
      List<Integer> forged = List.of(0, 1, 5);

      context.assertUnchanged(
          () -> world.containerSnapshot(chest.pos()),
          () -> InvGameTests.act(context,
              mc -> TroveNetworking.sendToServer(new Networking.SortC2S(false, forged, 0L))));
    }
  }
}
