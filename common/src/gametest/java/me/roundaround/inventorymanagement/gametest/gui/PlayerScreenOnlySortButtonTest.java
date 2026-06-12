package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * GUI-02: the player inventory ({@code InventoryScreen}) shows exactly ONE mod button, a
 * {@link SortInventoryButton}. The container-side buttons are skipped on {@code InventoryScreen}
 * (see {@code InventoryButtonsManager#generateSortButton} et al. returning early when
 * {@code screen instanceof InventoryScreen && !isPlayerInventory}), and the player-side stack /
 * transfer buttons need a supported container inventory that does not exist here (the player's own
 * crafting result container is not transferable) — so only the player-side sort button survives.
 *
 * <p>We open the screen by constructing it and calling {@code setScreen} rather than
 * {@code ClientWorld#openInventory()}: the keybind-driven open proved unreliable in this harness,
 * whereas {@code setScreen} runs {@code Screen.init} which fires Trove's {@code afterInit} hook
 * (and thus {@code InventoryButtonsManager}) exactly like a real open.
 */
@ClientGameTest
public class PlayerScreenOnlySortButtonTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
      context.waitTicks(2);

      int total = context.widgets(InventoryManagementButton.class).size();
      int sort = context.widgets(SortInventoryButton.class).size();

      if (total != 1 || sort != 1) {
        throw new GameTestAssertionException(
            "expected exactly 1 mod button (a SortInventoryButton) on the player inventory, got "
                + total + " total / " + sort + " sort");
      }
    }
  }
}
