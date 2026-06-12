package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.gui.AutoStackButton;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.inventorymanagement.client.gui.TransferAllButton;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;

/**
 * GUI-05 (showTransfer off): with {@code showTransfer} disabled before opening a chest, zero
 * transfer-all buttons appear while sort and stack remain at two each. The flag is read at
 * screen-init in {@code generateTransferAllButton}, so it is set before opening and restored on
 * cleanup.
 */
@ClientGameTest
public class ShowTransferOffTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.withBoolean(context, c -> c.showTransfer, false);

      InvGameTests.openChest(context, world);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();

      if (sort != 2 || stack != 2 || transfer != 0) {
        throw new GameTestAssertionException(
            "with showTransfer off expected 2 sort / 2 stack / 0 transfer buttons, got "
                + sort + "/" + stack + "/" + transfer);
      }
    }
  }
}
