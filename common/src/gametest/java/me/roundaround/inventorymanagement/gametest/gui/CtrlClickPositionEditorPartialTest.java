package me.roundaround.inventorymanagement.gametest.gui;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.Modifier;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * GUI-09 (PARTIAL — limitation documented): Ctrl+clicking a mod button is meant to open the
 * per-screen position editor instead of running the button's action. This branch CANNOT be driven
 * from a gametest today: {@code InventoryManagementButton} decides via the GLOBAL key state
 * ({@code Minecraft#hasControlDown()}, i.e. real GLFW keys), but Trove's
 * {@code clickWidget(widget, Set.of(Modifier.CTRL))} only rides the modifier bits on the synthetic
 * click event — it does not make {@code hasControlDown()} return true. So a synthetic "Ctrl"+click
 * takes the NORMAL (sort) branch.
 *
 * <p>This test asserts the feasible, observable consequence: a synthetic Ctrl+click on the sort
 * buttons does NOT open the editor (the chest screen stays up) and DOES perform the sort. It exists
 * to exercise the widget-click path and to pin the limitation.
 *
 * <p>TODO: promote to a real assertion that {@code PerScreenPositionEditScreen} opens once either
 * Trove can drive {@code hasControlDown()} (a real Ctrl key-hold across the click) or the button is
 * changed to read the click event's modifiers instead of the global key state.
 */
@ClientGameTest
public class CtrlClickPositionEditorPartialTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      world.setContainerItem(chest.pos(), 0, new ItemStack(Items.STONE));
      world.setContainerItem(chest.pos(), 1, new ItemStack(Items.APPLE));
      world.setContainerItem(chest.pos(), 2, new ItemStack(Items.DIRT));
      context.waitTicks(2);

      // Synthetic Ctrl+click every sort button. Because the event-modifier does not reach the global
      // hasControlDown() the button reads, this takes the normal sort branch rather than opening the editor.
      for (SortInventoryButton button : context.widgets(SortInventoryButton.class)) {
        context.clickWidget(button, Set.of(Modifier.CTRL));
      }
      context.waitTicks(InvGameTests.SETTLE_TICKS);

      // Feasible assertion #1: the editor did NOT open — the chest screen is still up.
      context.assertScreen(ContainerScreen.class);

      // Feasible assertion #2: the click reached the button's normal action, so the container sorted.
      List<ItemStack> after = world.containerSnapshot(chest.pos());
      assertSlot(after, 0, Items.APPLE, 1);
      assertSlot(after, 1, Items.DIRT, 1);
      assertSlot(after, 2, Items.STONE, 1);
    }
  }
}
