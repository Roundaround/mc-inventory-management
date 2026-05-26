package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.api.sorting.SortComparatorRegistry;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;

/**
 * The tie-break slot in {@code ItemStackComparator} backed by the public
 * {@link SortComparatorRegistry}. A live {@link SerialComparator} (first-non-zero-wins) over the
 * registry's priority-ordered contributions, re-read on every comparison so the reused singleton
 * never freezes a stale snapshot.
 *
 * <p>When the registry is empty {@link SortComparatorRegistry#comparators()} returns an empty list,
 * so {@link SerialComparator#compare} iterates nothing and returns {@code 0} for every pair &mdash;
 * byte-for-byte identical to the historical no-op delegate. The slot only votes once a mod registers
 * a contribution.
 */
public class RegistryBackedComparator implements SerialComparator<ItemStack> {
  private static RegistryBackedComparator instance;

  private RegistryBackedComparator() {
  }

  public static RegistryBackedComparator getInstance() {
    if (instance == null) {
      instance = new RegistryBackedComparator();
    }
    return instance;
  }

  @Override
  public @NotNull Iterator<Comparator<ItemStack>> iterator() {
    return SortComparatorRegistry.comparators().iterator();
  }
}
