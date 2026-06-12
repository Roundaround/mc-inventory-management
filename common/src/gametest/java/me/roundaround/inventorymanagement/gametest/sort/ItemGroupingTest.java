package me.roundaround.inventorymanagement.gametest.sort;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * SORT-08: item grouping clusters variant families (here, all wool colors) together regardless of
 * each member's own name. Seeds a chest with interleaved wool colors plus an unrelated item whose
 * name ("Iron Ingot") sorts among the wool color names. With {@code itemGrouping} on the wool
 * variants are contiguous (the iron ingot lands outside the cluster); flipping {@code itemGrouping}
 * off re-sorts each wool by its own name so the iron ingot interleaves and the wools are no longer
 * contiguous.
 */
@ClientGameTest
public class ItemGroupingTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      InvGameTests.Opened chest = InvGameTests.openChest(context, world);

      // --- Phase 1: grouping ON (default) -> all wool variants contiguous ---
      InvGameTests.withBoolean(context, c -> c.itemGrouping, true);
      seed(world, chest.pos());
      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> grouped = world.containerSnapshot(chest.pos());
      if (!woolContiguous(grouped)) {
        throw new GameTestAssertionException(
            "with itemGrouping on, all wool variants should be contiguous but were not: " + describe(grouped));
      }

      // --- Phase 2: grouping OFF -> wools sort by own name, iron ingot interleaves ---
      clear(world, chest.pos());
      InvGameTests.withBoolean(context, c -> c.itemGrouping, false);
      seed(world, chest.pos());
      InvGameTests.act(context, mc -> ClientNetworking.sendSort(mc.player, false));

      List<ItemStack> ungrouped = world.containerSnapshot(chest.pos());
      if (woolContiguous(ungrouped)) {
        throw new GameTestAssertionException(
            "with itemGrouping off, the iron ingot should interleave the wools (breaking contiguity) but they were still contiguous: "
                + describe(ungrouped));
      }
    }
  }

  private static void seed(ClientWorld world, BlockPos pos) {
    // Interleaved wool colors plus iron ingot; deliberately not pre-sorted.
    world.setContainerItem(pos, 0, new ItemStack(Items.RED_WOOL));
    world.setContainerItem(pos, 1, new ItemStack(Items.IRON_INGOT));
    world.setContainerItem(pos, 2, new ItemStack(Items.BLACK_WOOL));
    world.setContainerItem(pos, 3, new ItemStack(Items.WHITE_WOOL));
    world.setContainerItem(pos, 4, new ItemStack(Items.GREEN_WOOL));
    world.setContainerItem(pos, 5, new ItemStack(Items.BLUE_WOOL));
    world.context().waitTicks(2);
  }

  private static void clear(ClientWorld world, BlockPos pos) {
    for (int i = 0; i < 6; i++) {
      world.setContainerItem(pos, i, ItemStack.EMPTY);
    }
    world.context().waitTicks(2);
  }

  /** True if every wool item in the snapshot occupies one unbroken run of slots. */
  private static boolean woolContiguous(List<ItemStack> snapshot) {
    int firstWool = -1;
    int lastWool = -1;
    int woolCount = 0;
    for (int i = 0; i < snapshot.size(); i++) {
      if (isWool(snapshot.get(i).getItem())) {
        if (firstWool == -1) {
          firstWool = i;
        }
        lastWool = i;
        woolCount++;
      }
    }
    // Contiguous iff the wool count equals the span between the first and last wool slot.
    return woolCount > 0 && (lastWool - firstWool + 1) == woolCount;
  }

  private static boolean isWool(Item item) {
    return item == Items.RED_WOOL || item == Items.BLACK_WOOL || item == Items.WHITE_WOOL
        || item == Items.GREEN_WOOL || item == Items.BLUE_WOOL;
  }

  private static String describe(List<ItemStack> snapshot) {
    StringBuilder sb = new StringBuilder("[");
    for (ItemStack stack : snapshot) {
      if (!stack.isEmpty()) {
        sb.append(stack.getItem()).append(' ');
      }
    }
    return sb.append(']').toString();
  }
}
