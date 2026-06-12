package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * SORT-10: sorting an open ender chest reorders the player's ender-chest inventory. The ender chest
 * menu is backed by the player's {@code PlayerEnderChestContainer}, so a container-side sort routes
 * through it. Seeds the ender chest, opens the {@link ContainerScreen} over a placed ender-chest
 * block, sorts the container side, and asserts the ender-chest snapshot is alphabetical.
 */
@ClientGameTest
public class EnderChestSortTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openContainer(context, world, Blocks.ENDER_CHEST, ContainerScreen.class);

      world.setEnderChestItem(0, new ItemStack(Items.STONE));
      world.setEnderChestItem(1, new ItemStack(Items.APPLE));
      world.setEnderChestItem(2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> after = world.enderChestSnapshot();
      assertSlot(after, 0, Items.APPLE, 1);
      assertSlot(after, 1, Items.DIRT, 1);
      assertSlot(after, 2, Items.STONE, 1);
    }
  }
}
