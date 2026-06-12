package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

/**
 * GUI-10: the tabbed creative inventory ({@code CreativeModeInventoryScreen}, NOT the survival
 * player inventory) shows ZERO mod buttons. {@code InventoryButtonsManager#onScreenAfterInit}
 * short-circuits on {@code CreativeModeInventoryScreen} before adding anything, so even the
 * otherwise-universal player-side sort button is suppressed: the screen is mostly an item picker
 * (sorting it would be meaningless) and only borrows the player inventory on its survival tab.
 *
 * <p>As in {@link PlayerScreenOnlySortButtonTest} we open the screen by constructing it and calling
 * {@code setScreen} (the keybind-driven open is unreliable in this harness); {@code Screen.init}
 * still fires Trove's {@code afterInit} hook exactly like a real open. The constructor mirrors how
 * vanilla {@code InventoryScreen} opens it for a creative player.
 */
@ClientGameTest
public class CreativeInventoryNoButtonsTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      context.setScreen(() -> {
        Minecraft mc = Minecraft.getInstance();
        return new CreativeModeInventoryScreen(
            mc.player, mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get());
      });
      context.waitTicks(2);

      int total = InvGameTests.buttons(context).size();
      if (total != 0) {
        throw new GameTestAssertionException(
            "expected ZERO mod buttons on the creative inventory screen, got " + total);
      }
    }
  }
}
