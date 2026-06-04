package me.roundaround.inventorymanagement.client;

import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.durability.DurabilityReplace;
import me.roundaround.trove.event.ClientLifecycle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side driver for the durability features, run from a single per-tick handler registered in
 * {@link #init()}. Both features are entirely <b>client-driven</b> — there is NO client/server preference
 * sync. The client owns the config, watches durability (which the server already syncs to it), and either
 * shows a local alert or sends a one-shot action request the server validates.
 *
 * <ol>
 *   <li><b>Low-durability alert</b> (purely client-side): polls the player's six equippable slots and
 *       fires an action-bar message + anvil ping once per threshold crossed downward. Works even on
 *       vanilla servers.</li>
 *   <li><b>Auto-replace before break</b>: when an equipped item reaches its last durability point, the
 *       client picks a matching replacement from its own inventory and sends a single swap request
 *       ({@link ClientNetworking#sendDurabilityReplace}); the server re-validates and performs the swap.
 *       The mod must be installed on the server for this to take effect in multiplayer.</li>
 * </ol>
 *
 * <p>Per-slot state tracks {@code (itemIdentity, previousRemaining, replaceRequested)} so alerts fire on a
 * genuine downward crossing and re-arm on an item swap, and a replace request is sent at most once per
 * about-to-break item.
 */
public final class DurabilityClient {
  /** The slots polled for both features: both hands plus the four armor slots (elytra rides in CHEST). */
  private static final EquipmentSlot[] SLOTS = {
      EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };

  private static final Map<EquipmentSlot, SlotState> SLOT_STATES = new EnumMap<>(EquipmentSlot.class);

  private static boolean initialized = false;

  private DurabilityClient() {
  }

  private static InventoryManagementConfig config() {
    return InventoryManagementConfig.getInstance();
  }

  /** Wire up the per-tick handler. Idempotent; safe to call from each loader's client-init site. */
  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;
    ClientLifecycle.onTick(DurabilityClient::tick);
  }

  private static void tick() {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      if (!SLOT_STATES.isEmpty()) {
        SLOT_STATES.clear();
      }
      return;
    }
    if (!config().isInitialized()) {
      return;
    }

    boolean alertEnabled = config().durabilityAlertEnabled.getValue();
    boolean alertAtOne = config().durabilityAlertAtOne.getValue();
    boolean playSound = config().durabilityAlertSound.getValue();
    List<Integer> thresholds = config().getDurabilityAlertThresholds();
    boolean autoReplace = config().durabilityAutoReplace.getValue();
    boolean similar = config().durabilityAutoReplaceSimilar.getValue();

    for (EquipmentSlot slot : SLOTS) {
      pollSlot(player, slot, alertEnabled, thresholds, alertAtOne, playSound, autoReplace, similar);
    }
  }

  private static void pollSlot(
      LocalPlayer player, EquipmentSlot slot, boolean alertEnabled, List<Integer> thresholds,
      boolean alertAtOne, boolean playSound, boolean autoReplace, boolean similar
  ) {
    ItemStack stack = player.getItemBySlot(slot);

    if (!stack.isDamageableItem()) {
      SLOT_STATES.remove(slot);
      return;
    }
    int max = stack.getMaxDamage();
    if (max <= 0) {
      SLOT_STATES.remove(slot);
      return;
    }

    int remaining = max - stack.getDamageValue();
    // Identity must be STABLE while an item takes damage. The client replaces a slot's ItemStack object on
    // every durability sync, so the registry-singleton Item is used (NOT the stack reference, which would
    // make every damage tick look like a swap). It still changes on a genuine swap to a different item.
    Object identity = stack.getItem();
    SlotState prev = SLOT_STATES.get(slot);
    boolean sameItem = prev != null && prev.identity == identity;

    // --- Low-durability alert: fire once per downward threshold crossing on the same item. ---
    if (alertEnabled && sameItem && remaining < prev.remaining) {
      int prevPercent = prev.remaining * 100 / max;
      int curPercent = remaining * 100 / max;
      boolean fire = false;
      for (int threshold : thresholds) {
        if (prevPercent > threshold && curPercent <= threshold) {
          fire = true;
        }
      }
      if (alertAtOne && remaining == 1 && prev.remaining > 1) {
        fire = true;
      }
      if (fire) {
        alert(player, stack, remaining, max, playSound);
      }
    }

    // --- Auto-replace: request a swap once when the item reaches its last durability point. ---
    boolean requested = sameItem && prev.replaceRequested;
    if (autoReplace && stack.nextDamageWillBreak()) {
      if (!requested) {
        DurabilityReplace.Replacement replacement =
            DurabilityReplace.findReplacement(player, stack, backingInventorySlot(player, slot), similar);
        if (replacement != null) {
          ClientNetworking.sendDurabilityReplace(replacement.slot(), slot, similar);
          requested = true;
        }
      }
    } else {
      // Re-arm once the item is no longer about to break (e.g. repaired or replaced).
      requested = false;
    }

    SLOT_STATES.put(slot, new SlotState(identity, remaining, requested));
  }

  /** The combined-inventory index backing {@code slot} (selected hotbar slot / offhand), or -1 for armor. */
  private static int backingInventorySlot(LocalPlayer player, EquipmentSlot slot) {
    return switch (slot) {
      case MAINHAND -> player.getInventory().getSelectedSlot();
      case OFFHAND -> Inventory.SLOT_OFFHAND;
      default -> -1;
    };
  }

  private static void alert(LocalPlayer player, ItemStack stack, int remaining, int max, boolean playSound) {
    MutableComponent name = Component.empty().append(stack.getHoverName()).withStyle(ChatFormatting.GOLD);
    Component message = Component.translatable(
            "inventorymanagement.durability.alert",
            name,
            Component.literal(Integer.toString(remaining)),
            Component.literal(Integer.toString(max)))
        .withStyle(ChatFormatting.RED);

    player.sendOverlayMessage(message);

    if (playSound) {
      player.level().playLocalSound(
          player.getX(), player.getY(), player.getZ(),
          SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 2.0f, false);
    }
  }

  private record SlotState(Object identity, int remaining, boolean replaceRequested) {
  }
}
