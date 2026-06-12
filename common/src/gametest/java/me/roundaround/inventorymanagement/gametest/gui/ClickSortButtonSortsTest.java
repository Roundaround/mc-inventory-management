package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * GUI-08: clicking the container-side {@link SortInventoryButton} actually sorts the container,
 * exercising the real widget -&gt; {@code onPress} -&gt; C2S -&gt; server -&gt; re-sync path (not the
 * {@code ClientNetworking} shortcut). Seeds {@code [stone, apple, dirt]}, clicks EVERY sort button
 * (the player-side one is a harmless no-op on the empty player grid), waits for the round trip, and
 * asserts the chest is reordered alphabetically to {@code [apple, dirt, stone]}.
 */
@ClientGameTest
public class ClickSortButtonSortsTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      for (SortInventoryButton button : context.widgets(SortInventoryButton.class)) {
        context.clickWidget(button);
      }
      context.waitTicks(InvGameTests.SETTLE_TICKS);

      List<ItemStack> after = world.containerSnapshot(chest.pos());
      assertSlot(after, 0, Items.APPLE, 1);
      assertSlot(after, 1, Items.DIRT, 1);
      assertSlot(after, 2, Items.STONE, 1);
    }
  }
}
