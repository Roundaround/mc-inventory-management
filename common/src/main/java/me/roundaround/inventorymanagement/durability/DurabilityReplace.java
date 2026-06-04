package me.roundaround.inventorymanagement.durability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Shared (client + server) matching logic for the auto-replace feature. The feature is <em>client-driven</em>:
 * the CLIENT uses {@link #findReplacement} to choose a replacement from its own inventory and then sends a
 * swap request; the SERVER re-uses {@link #matches} to validate that request before performing the swap.
 *
 * <p>No state, no config, no networking here — purely the "does this candidate replace that item" decision,
 * so it can run identically on both sides (a {@code Player} is all it needs; the client passes its
 * {@code LocalPlayer}). There is intentionally no preference sync: the client decides per-event and the
 * request carries everything the server needs (slot indices + the strict/similar flag).
 */
public final class DurabilityReplace {
  /** Tool tags consulted for "similar" matching, in addition to armor-slot / glider checks. */
  private static final TagKey<Item>[] TOOL_TAGS = makeToolTags();

  @SuppressWarnings("unchecked")
  private static TagKey<Item>[] makeToolTags() {
    return new TagKey[] {
        ItemTags.SWORDS, ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.HOES
    };
  }

  private DurabilityReplace() {
  }

  /**
   * Finds the best replacement for {@code target} among the player's main inventory (slots 0-35) and the
   * offhand, skipping {@code excludeSlot} (the inventory slot backing the target itself, or {@code -1} when
   * the target is not in that range, e.g. an armor slot) and preferring the candidate with the most
   * remaining durability.
   *
   * @return the chosen replacement (inventory slot + stack), or {@code null} if none matches
   */
  public static Replacement findReplacement(Player player, ItemStack target, int excludeSlot, boolean similar) {
    Inventory inventory = player.getInventory();

    Replacement best = null;
    int bestRemaining = -1;

    for (int slot : candidateSlots()) {
      if (slot == excludeSlot) {
        continue;
      }
      ItemStack candidate = inventory.getItem(slot);
      if (candidate.isEmpty() || !candidate.isDamageableItem()) {
        continue;
      }
      if (!matches(player, target, candidate, similar)) {
        continue;
      }

      int remaining = candidate.getMaxDamage() - candidate.getDamageValue();
      if (remaining > bestRemaining) {
        bestRemaining = remaining;
        best = new Replacement(slot, candidate);
      }
    }

    return best;
  }

  private static int[] candidateSlots() {
    // Main inventory slots 0-35 plus the offhand (Inventory routes SLOT_OFFHAND through getItem/setItem).
    int[] slots = new int[37];
    for (int i = 0; i < 36; i++) {
      slots[i] = i;
    }
    slots[36] = Inventory.SLOT_OFFHAND;
    return slots;
  }

  /**
   * Whether {@code candidate} may replace {@code broken}. Strict (default) requires the same {@link Item}
   * and equal enchantments; similar relaxes to category: any glider for a glider, any item equippable to
   * the same armor slot, any item sharing one of the broken tool's tool tags, or (fallback) the same item.
   */
  public static boolean matches(Player player, ItemStack broken, ItemStack candidate, boolean similar) {
    if (!similar) {
      return candidate.getItem() == broken.getItem()
          && candidate.getEnchantments().equals(broken.getEnchantments());
    }

    if (broken.has(DataComponents.GLIDER) && candidate.has(DataComponents.GLIDER)) {
      return true;
    }
    EquipmentSlot brokenSlot = player.getEquipmentSlotForItem(broken);
    if (brokenSlot.isArmor() && player.getEquipmentSlotForItem(candidate) == brokenSlot) {
      return true;
    }
    for (TagKey<Item> tag : TOOL_TAGS) {
      if (isInTag(broken, tag) && isInTag(candidate, tag)) {
        return true;
      }
    }
    return candidate.getItem() == broken.getItem();
  }

  private static boolean isInTag(ItemStack stack, TagKey<Item> tag) {
    return stack.is(holder -> holder.is(tag));
  }

  /** A located replacement candidate: the inventory slot it came from and the stack itself. */
  public record Replacement(int slot, ItemStack stack) {
  }
}
