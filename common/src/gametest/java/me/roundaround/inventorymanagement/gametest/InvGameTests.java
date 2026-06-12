package me.roundaround.inventorymanagement.gametest;

import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.trove.config.option.BooleanConfigOption;
import me.roundaround.trove.gametest.ClientMenu;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Shared scaffolding for the inventory-management client gametests. Centralizes the five-beat
 * pattern (world &rarr; place &amp; open &rarr; seed &rarr; act &rarr; assert) and the Trove 1.1.0 primitives the
 * suite leans on so the per-feature classes stay declarative.
 *
 * <p>Conventions used throughout the suite:
 * <ul>
 *   <li>Worlds are creative ({@code worldBuilder().creative().stopTime(true)}) so the player
 *       inventory is a real, empty {@code Inventory} and the hotbar is empty at open time — a bare
 *       right-click then opens the container instead of placing/using a held item.</li>
 *   <li><strong>Open the container first, then seed</strong> via {@link ClientWorld#setContainerItem}
 *       / {@link ClientWorld#setInventoryItem}; the seeds write the authoritative server state and
 *       sync into the already-open menu within a tick.</li>
 *   <li>Fire the action with {@link #act} (sends the real C2S packet and waits for the round trip),
 *       then read back with {@link ClientWorld#containerSnapshot} / {@link ClientWorld#inventorySnapshot}
 *       and assert with {@code GameTestAssertions}.</li>
 * </ul>
 */
public final class InvGameTests {
  /** Ticks to wait after a client action for the C2S packet to reach the server, mutate, and re-sync. */
  public static final int SETTLE_TICKS = 4;

  private InvGameTests() {
  }

  /** The container handle returned by the open helpers: the block position and its open {@link ClientMenu}. */
  public record Opened(BlockPos pos, ClientMenu menu) {
  }

  /** Place a chest two blocks south of the player, open it, and return the position + menu. */
  public static Opened openChest(ClientTestContext context, ClientWorld world) {
    return openContainer(context, world, Blocks.CHEST, ContainerScreen.class);
  }

  /** Place {@code block} two blocks south of the player, open it as {@code screenClass}, return the handle. */
  public static Opened openContainer(
      ClientTestContext context,
      ClientWorld world,
      Block block,
      Class<? extends AbstractContainerScreen<?>> screenClass
  ) {
    BlockPos pos = world.playerBlockPos().south(2);
    world.setBlock(pos, block);
    context.waitTicks(2);
    ClientMenu menu = world.openMenu(pos, screenClass);
    return new Opened(pos, menu);
  }

  /** Run a client-side action (typically a {@code ClientNetworking.send*}) and wait for the round trip. */
  public static void act(ClientTestContext context, Consumer<Minecraft> action) {
    context.runOnClient(action);
    context.waitTicks(SETTLE_TICKS);
  }

  /** A stack of {@code count} {@code item}s tagged with a {@code CUSTOM_NAME} marker, to track it across an op. */
  public static ItemStack named(Item item, int count, String name) {
    ItemStack stack = new ItemStack(item, count);
    stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    return stack;
  }

  /** All inventory-management buttons currently on the open screen (both player- and container-side, all types). */
  public static List<InventoryManagementButton> buttons(ClientTestContext context) {
    return context.widgets(InventoryManagementButton.class);
  }

  /**
   * Lock the given player main-inventory slots through the mod config (the same path Ctrl+click uses), and
   * register cleanup that unlocks them again. Slots are expected to start unlocked (fresh test world).
   */
  public static void lockSlots(ClientTestContext context, int... slots) {
    toggleLocks(context, slots);
    context.onCleanup(() -> toggleLocks(context, slots));
  }

  private static void toggleLocks(ClientTestContext context, int... slots) {
    context.runOnClient(mc -> {
      InventoryManagementConfig config = InventoryManagementConfig.getInstance();
      for (int slot : slots) {
        config.toggleLockedPlayerSlot(slot);
      }
    });
  }

  /**
   * Temporarily set a boolean config option for the duration of the test, restoring the prior value on cleanup.
   *
   * <p>{@code setValue} only stages a <em>pending</em> value; the mod reads {@code getValue()} (the committed
   * "saved" value), so we must {@code commit()} for the change to actually take effect — both when applying and
   * when restoring.
   */
  public static void withBoolean(
      ClientTestContext context, Function<InventoryManagementConfig, BooleanConfigOption> option, boolean value
  ) {
    context.runOnClient(mc -> {
      BooleanConfigOption opt = option.apply(InventoryManagementConfig.getInstance());
      boolean previous = opt.getValue();
      opt.setValue(value);
      opt.commit();
      context.onCleanup(() -> context.runOnClient(m -> {
        BooleanConfigOption restore = option.apply(InventoryManagementConfig.getInstance());
        restore.setValue(previous);
        restore.commit();
      }));
    });
  }
}
