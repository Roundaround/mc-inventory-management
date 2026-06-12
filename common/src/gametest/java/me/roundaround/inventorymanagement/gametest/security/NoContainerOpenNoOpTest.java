package me.roundaround.inventorymanagement.gametest.security;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * SEC-03: container-targeting ops fired with no real container open never disturb the player's main
 * inventory. With nothing but the player's own inventory screen backing {@code player.containerMenu},
 * the "container" the server resolves is the crafting {@code ResultContainer} (a single, place-rejecting
 * slot), so every container op degenerates to a no-op for slots 0-35: an auto-stack only grows existing
 * destination stacks (the result slot is empty), a transfer-from-player is blocked by the result slot's
 * {@code mayPlace} returning false, and the container ops sourced from the (empty) result container have
 * nothing to move. This test seeds a known player layout, fires
 * {@code sendStack(true)/sendStack(false)/sendTransfer(true)/sendTransfer(false)/sendSort(player,false)}
 * without ever opening a container, and asserts the player main inventory is byte-for-byte unchanged.
 *
 * <p>Note: the GAMETEST_PLAN frames this as "the server bails when getContainerInventory is null"; in
 * the no-screen case {@code getContainerInventory} actually returns the result container rather than
 * null, but the observable outcome — the player inventory is left alone — is the same, so this test
 * asserts that ground-truth behavior directly.
 */
@ClientGameTest
public class NoContainerOpenNoOpTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // Deliberately do NOT open any container. Seed a known player layout (main-grid slots 9-35).
      world.setInventoryItem(9, new ItemStack(Items.DIAMOND, 5));
      world.setInventoryItem(14, new ItemStack(Items.STONE, 32));
      world.setInventoryItem(20, new ItemStack(Items.APPLE, 3));
      world.setInventoryItem(35, new ItemStack(Items.DIAMOND, 12));
      context.waitTicks(2);

      context.assertUnchanged(world::inventorySnapshot, () -> {
        InvGameTests.act(context, mc -> ClientNetworking.sendStack(true));
        InvGameTests.act(context, mc -> ClientNetworking.sendStack(false));
        InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(true));
        InvGameTests.act(context, mc -> ClientNetworking.sendTransfer(false));
        InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));
      });
    }
  }
}
