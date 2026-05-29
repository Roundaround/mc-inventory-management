package me.roundaround.inventorymanagement.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Declarative helpers for the Locked Slots feature. Locked slots are expressed as exclusions on the
 * player main-inventory {@link SlotRange}, so enforcement (sort / auto-stack / transfer-all) is a
 * single range construction at each site rather than scattered {@code contains} guards.
 *
 * <p>On the wire and in computation the locked set is a {@code long} bitmask: bit {@code n}
 * corresponds to absolute player-inventory slot {@code playerMainRange().min() + n}. A {@code long}
 * gives 64 bits of headroom, enough for the vanilla 27 main slots plus any inventory-expanding mod
 * up to 64 main slots. Only the persisted config keeps the readable {@code List<Integer>} of
 * absolute indices; this class converts between the two at the boundary.
 */
public final class IgnoredSlots {
  private IgnoredSlots() {
  }

  /**
   * The player main-inventory range with the slots flagged in {@code lockedMask} excluded. Pass this
   * everywhere a player range is built so locked slots are uniformly skipped. A {@code 0} mask yields
   * the full player main range.
   */
  public static SlotRange playerLockedRange(long lockedMask) {
    SlotRange main = SlotRange.playerMainRange();
    if (lockedMask == 0L) {
      return main;
    }

    List<Integer> excluded = new ArrayList<>();
    for (int idx = main.min(); idx < main.max(); idx++) {
      int bit = idx - main.min();
      if (bit < Long.SIZE && (lockedMask & (1L << bit)) != 0L) {
        excluded.add(idx);
      }
    }
    return main.withExclusions(excluded);
  }

  /**
   * Encode a collection of absolute player-inventory indices into a {@code long} bitmask. Indices
   * outside the lockable player main range (container, hotbar, or beyond bit 63) are silently
   * ignored, so the mask only ever describes lockable slots.
   */
  public static long maskOf(Collection<Integer> lockedIndices) {
    SlotRange main = SlotRange.playerMainRange();
    int min = main.min();
    long mask = 0L;
    for (int idx : lockedIndices) {
      int bit = idx - min;
      if (bit >= 0 && bit < Long.SIZE && main.contains(idx)) {
        mask |= (1L << bit);
      }
    }
    return mask;
  }

  /**
   * Whether {@code idx} is an absolute player-inventory index that may be locked, i.e. a main
   * (non-hotbar) player slot. Container, shulker, ender, and hotbar slots are not lockable in v1.
   */
  public static boolean isLockable(int idx) {
    return SlotRange.playerMainRange().contains(idx);
  }
}
