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
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.level.block.Blocks;

/**
 * GUI-03: an unsupported container (a crafting table) shows no <em>container-side</em> buttons —
 * {@code CraftingMenu} is in no allowlist. It DOES still show the single player-side SORT button,
 * which {@code InventoryButtonsManager} adds on any {@code AbstractContainerScreen} with a sortable
 * player inventory (the player-side auto-stack/transfer need a supported container, so they stay
 * hidden). Net result: total 1, a lone {@link SortInventoryButton}. (The plan's "zero buttons"
 * wording was wrong; the player-side sort is always available.)
 */
@ClientGameTest
public class UnsupportedContainerPlayerSortOnlyTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.openContainer(context, world, Blocks.CRAFTING_TABLE, CraftingScreen.class);

      int sort = context.widgets(SortInventoryButton.class).size();
      int stack = context.widgets(AutoStackButton.class).size();
      int transfer = context.widgets(TransferAllButton.class).size();
      int total = InvGameTests.buttons(context).size();

      if (sort != 1 || stack != 0 || transfer != 0 || total != 1) {
        throw new GameTestAssertionException(
            "expected a crafting table to show only the player-side sort button (1/0/0, total 1), got "
                + sort + "/" + stack + "/" + transfer + " (total " + total + ")");
      }
    }
  }
}
