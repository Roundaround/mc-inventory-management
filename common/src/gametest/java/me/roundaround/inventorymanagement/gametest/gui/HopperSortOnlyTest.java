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
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.world.level.block.Blocks;

/**
 * GUI-06: a hopper shows the two SORT buttons (player + container) but NO auto-stack / transfer
 * buttons — total 2. This is the deliberate allowlist asymmetry: when a container presents through
 * the {@code SimpleContainer} branch, {@code InventoryButtonsManager} gates it on the screen-handler
 * allowlists, and {@code HopperMenu} is in {@code sortableScreenHandlers} but NOT in
 * {@code transferableScreenHandlers}. (An earlier draft of the plan and a first cut of this test
 * wrongly expected all six; the real run proved sort-only, matching the original plan.)
 */
@ClientGameTest
public class HopperSortOnlyTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openContainer(context, world, Blocks.HOPPER, HopperScreen.class);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();
      int total = context.widgets(InventoryManagementButton.class).size();

      if (sort != 2 || stack != 0 || transfer != 0 || total != 2) {
        throw new GameTestAssertionException(
            "expected a hopper to show 2 sort / 0 stack / 0 transfer buttons (total 2), got "
                + sort + "/" + stack + "/" + transfer + " (total " + total + ")");
      }
    }
  }
}
