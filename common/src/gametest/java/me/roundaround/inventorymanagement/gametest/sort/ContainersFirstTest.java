package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

/**
 * SORT-06: with {@code containersFirst} on, content-holding items sort ahead of everything else.
 * The {@code ContainerFirstComparator} keys on {@link DataComponents#CONTAINER}, so we build a
 * shulker-box stack carrying a non-empty {@code CONTAINER} component (a dirt item inside) and seed a
 * chest with {@code [apple, shulker, dirt]}. After the sort the shulker lands in slot 0 even though
 * "Apple"/"Dirt" precede "Shulker Box" alphabetically.
 */
@ClientGameTest
public class ContainersFirstTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      // Alphabetical mode is the default; containersFirst only applies there. Turn it on.
      InvGameTests.withBoolean(context, c -> c.containersFirst, true);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 1, containerItem());
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> after = world.containerSnapshot(chest.pos());
      ItemStack first = after.get(0);
      if (!first.has(DataComponents.CONTAINER)) {
        throw new GameTestAssertionException(
            "containersFirst should sort the container-bearing item to slot 0, but slot 0 held "
                + (first.isEmpty() ? "<empty>" : first.getCount() + "x " + first.getItem())
                + " (has CONTAINER=" + first.has(DataComponents.CONTAINER) + ")");
      }
      if (first.getItem() != Items.SHULKER_BOX) {
        throw new GameTestAssertionException(
            "expected the shulker box in slot 0 but found " + first.getItem());
      }
    }
  }

  /** A shulker-box stack carrying a non-empty {@code CONTAINER} component, so it reads as a container. */
  private static ItemStack containerItem() {
    ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
    shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIRT))));
    if (!shulker.has(DataComponents.CONTAINER)) {
      throw new GameTestAssertionException("test setup failed: shulker box did not carry a CONTAINER component");
    }
    return shulker;
  }
}
