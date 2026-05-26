package me.roundaround.inventorymanagement.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

import java.util.*;

public class SlotRange {
  private final int min;
  private final int max;
  private final int size;
  private final List<Integer> included;
  private final HashSet<Integer> includedSet;

  public SlotRange(int min, int max) {
    this(min, max, List.of());
  }

  public SlotRange(int min, int max, Collection<Integer> exclusions) {
    if (max < min) {
      max = min;
    }

    this.min = min;
    this.max = max;

    ArrayList<Integer> included = new ArrayList<>();
    HashSet<Integer> uniqueExclusions = new HashSet<>(exclusions);
    for (int i = min; i < max; i++) {
      if (!uniqueExclusions.contains(i)) {
        included.add(i);
      }
    }

    this.size = included.size();
    this.included = List.copyOf(included);
    this.includedSet = new HashSet<>(this.included);
  }

  public static SlotRange byMax(int min, int max) {
    return new SlotRange(min, max);
  }

  public static SlotRange bySize(int min, int size) {
    return new SlotRange(min, min + size);
  }

  public static SlotRange fullRange(Container inventory) {
    return byMax(0, inventory.getContainerSize());
  }

  public static SlotRange playerMainRange() {
    return byMax(Inventory.getSelectionSize(), Inventory.INVENTORY_SIZE);
  }

  public static SlotRange horseMainRange(Container inventory) {
    return byMax(2, inventory.getContainerSize());
  }

  public static SlotRange empty() {
    return new SlotRange(0, 0);
  }

  public int min() {
    return this.min;
  }

  public int max() {
    return this.max;
  }

  public int size() {
    return this.size;
  }

  public boolean contains(int slot) {
    return this.includedSet.contains(slot);
  }

  public SlotRange withExclusions(List<Integer> exclusions) {
    return new SlotRange(this.min, this.max, List.copyOf(exclusions));
  }

  public List<Integer> getSlots() {
    return this.included;
  }

  public <T> List<T> project(List<T> values, T empty) {
    @SuppressWarnings("unchecked")
    T[] projected = (T[]) new Object[this.max];
    Iterator<T> valuesIter = values.iterator();

    for (int i = 0; i < this.max; i++) {
      projected[i] = this.contains(i) && valuesIter.hasNext() ? valuesIter.next() : empty;
    }

    return List.of(projected);
  }
}
