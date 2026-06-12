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
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.world.level.block.Blocks;

/**
 * GUI-07 (dispenser): a dispenser is NOT a supported container for inventory-management — its
 * {@code DispenserMenu} is in neither the sortable nor the transferable screen-handler allowlist, so
 * no container-side buttons appear. The only button shown is the player-side SORT button, which
 * {@code InventoryButtonsManager} adds on ANY {@code AbstractContainerScreen} whose player inventory
 * is sortable. Net result: total 1 (one sort, zero stack/transfer). (The plan's original "all six"
 * matrix entry was wrong; the real run shows dispensers/droppers are unsupported.)
 */
@ClientGameTest
public class DispenserPlayerSortOnlyTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openContainer(context, world, Blocks.DISPENSER, DispenserScreen.class);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();
      int total = context.widgets(InventoryManagementButton.class).size();

      // Only the always-present player-side sort button; the dispenser container itself is unsupported.
      if (sort != 1 || stack != 0 || transfer != 0 || total != 1) {
        throw new GameTestAssertionException(
            "expected a dispenser to show only the player-side sort button (1/0/0, total 1), got "
                + sort + "/" + stack + "/" + transfer + " (total " + total + ")");
      }
    }
  }
}
