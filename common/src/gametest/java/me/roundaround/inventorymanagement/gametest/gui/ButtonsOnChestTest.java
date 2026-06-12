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

/**
 * GUI-01: a single chest shows all six inventory-management buttons — {sort, auto-stack, transfer}
 * for both the player side and the container side — once the screen initializes.
 */
@ClientGameTest
public class ButtonsOnChestTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openChest(context, world);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();
      int total = context.widgets(InventoryManagementButton.class).size();

      if (sort != 2 || stack != 2 || transfer != 2 || total != 6) {
        throw new GameTestAssertionException(
            "expected 2/2/2 (total 6) sort/stack/transfer buttons on a chest, got "
                + sort + "/" + stack + "/" + transfer + " (total " + total + ")");
      }
    }
  }
}
