package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.SortMode;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static me.roundaround.trove.gametest.GameTestAssertions.assertSlot;

/**
 * SORT-07: alphabetical and creative sort modes produce different orderings for a set whose creative-tab
 * order disagrees with its name order. Alphabetically it is apple, granite, stone; in the creative menu
 * stone and granite lead the Building Blocks tab (stone first) and apple sits in Food &amp; Drink, so
 * creative order is stone, granite, apple. The player never opens the creative inventory screen here,
 * which is the case that used to degrade creative to name order (issue #65): vanilla builds the tab
 * contents only from that screen, so the sort must build them itself.
 *
 * <p>Also checks the creative search tree got populated by that build, since vanilla's screen skips its
 * own search-tree refresh when the tab contents are already current.
 */
@ClientGameTest
public class SortModeCreativeVsAlphabeticalTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      setSortMode(context, SortMode.ALPHABETICAL);
      seed(world, chest.pos());
      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> alphabetical = world.containerSnapshot(chest.pos());
      assertSlot(alphabetical, 0, Items.APPLE, 1);
      assertSlot(alphabetical, 1, Items.GRANITE, 1);
      assertSlot(alphabetical, 2, Items.STONE, 1);

      clear(world, chest.pos());
      setSortMode(context, SortMode.CREATIVE);
      seed(world, chest.pos());
      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> creative = world.containerSnapshot(chest.pos());
      assertSlot(creative, 0, Items.STONE, 1);
      assertSlot(creative, 1, Items.GRANITE, 1);
      assertSlot(creative, 2, Items.APPLE, 1);

      context.runOnClient(mc -> {
        if (mc.player.connection.searchTrees().creativeNameSearch().search("stone").isEmpty()) {
          throw new GameTestAssertionException("creative name search is empty after the sort built the tab contents");
        }
      });
    }
  }

  private static void seed(ClientWorld world, BlockPos pos) {
    // Scrambled so neither mode can pass by leaving the slots alone.
    world.setContainerItem(pos, 0, new ItemStack(Items.GRANITE));
    world.setContainerItem(pos, 1, new ItemStack(Items.STONE));
    world.setContainerItem(pos, 2, new ItemStack(Items.APPLE));
    world.context().waitTicks(2);
  }

  private static void clear(ClientWorld world, BlockPos pos) {
    for (int slot = 0; slot < 3; slot++) {
      world.setContainerItem(pos, slot, ItemStack.EMPTY);
    }
    world.context().waitTicks(2);
  }

  private static void setSortMode(ClientTestContext context, SortMode mode) {
    context.runOnClient(mc -> {
      InventoryManagementConfig config = InventoryManagementConfig.getInstance();
      SortMode previous = config.sortMode.getValue();
      // setValue only stages a pending value; the client sort reads getValue() (the saved value), so commit it.
      config.sortMode.setValue(mode);
      config.sortMode.commit();
      context.onCleanup(() -> context.runOnClient(m -> {
        InventoryManagementConfig.getInstance().sortMode.setValue(previous);
        InventoryManagementConfig.getInstance().sortMode.commit();
      }));
    });
  }
}
