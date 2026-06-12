package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.gui.AutoStackButton;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.inventorymanagement.client.gui.TransferAllButton;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.level.block.Blocks;

/**
 * GUI-07 (barrel): a barrel shows all six mod buttons. Its block entity is a
 * {@code RandomizableContainerBlockEntity} (sortable + transferable) opened through a
 * {@code ChestMenu}, rendered with the generic {@code ContainerScreen}, so it behaves like a chest.
 */
@ClientGameTest
public class BarrelButtonsTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openContainer(context, world, Blocks.BARREL, ContainerScreen.class);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();
      int total = context.widgets(InventoryManagementButton.class).size();

      if (sort != 2 || stack != 2 || transfer != 2 || total != 6) {
        throw new GameTestAssertionException(
            "expected all six buttons (2/2/2) on a barrel, got "
                + sort + "/" + stack + "/" + transfer + " (total " + total + ")");
      }
    }
  }
}
