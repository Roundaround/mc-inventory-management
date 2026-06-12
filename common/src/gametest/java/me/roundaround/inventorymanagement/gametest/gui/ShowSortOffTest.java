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
 * GUI-05 (showSort off): with {@code showSort} disabled before opening a chest, zero sort buttons
 * appear while the other two button types remain at their full count of two each. The flag is read
 * at screen-init in {@code generateSortButton}, so it is set before opening and restored on cleanup.
 */
@ClientGameTest
public class ShowSortOffTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.withBoolean(context, c -> c.showSort, false);

      InvGameTests.openChest(context, world);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();

      if (sort != 0 || stack != 2 || transfer != 2) {
        throw new GameTestAssertionException(
            "with showSort off expected 0 sort / 2 stack / 2 transfer buttons, got "
                + sort + "/" + stack + "/" + transfer);
      }
    }
  }
}
