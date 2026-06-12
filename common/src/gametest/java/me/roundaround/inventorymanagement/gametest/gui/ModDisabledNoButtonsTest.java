package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;

/**
 * GUI-04: with the master {@code modEnabled} toggle off, opening a chest produces no mod buttons.
 * Buttons are generated at screen-init reading config, so {@code modEnabled} is set to false BEFORE
 * the chest is opened (each {@code generate*Button} bails immediately when {@code modEnabled} is
 * false). The config value is restored automatically on cleanup by {@link InvGameTests#withBoolean}.
 */
@ClientGameTest
public class ModDisabledNoButtonsTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.withBoolean(context, c -> c.modEnabled, false);

      InvGameTests.openChest(context, world);

      int total = InvGameTests.buttons(context).size();
      if (total != 0) {
        throw new GameTestAssertionException(
            "expected no mod buttons when modEnabled is false, got " + total);
      }
    }
  }
}
