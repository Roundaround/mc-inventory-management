package me.roundaround.inventorymanagement.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortableInventory implements Container {
  private final Container source;
  private final NonNullList<ItemStack> stacks;

  public SortableInventory(Container source) {
    this.source = source;
    this.stacks = NonNullList.withSize(source.getContainerSize(), ItemStack.EMPTY);
    this.copyStacks();
  }

  @Override
  public int getContainerSize() {
    return this.stacks.size();
  }

  @Override
  public boolean isEmpty() {
    return this.stacks.isEmpty();
  }

  @Override
  public ItemStack getItem(int slot) {
    if (slot < 0 || slot >= this.stacks.size()) {
      return ItemStack.EMPTY;
    }
    return this.stacks.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    return this.getItem(slot).isEmpty() ? ItemStack.EMPTY : ContainerHelper.removeItem(this.stacks, slot, amount);
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack stack = this.getItem(slot);
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    this.stacks.set(slot, ItemStack.EMPTY);
    return stack;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    this.stacks.set(slot, stack);
  }

  @Override
  public void setChanged() {
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public void clearContent() {
    this.stacks.clear();
  }

  public List<Integer> sort(SlotRange slotRange, Comparator<ItemStack> comparator) {
    StacksList stacks = this.getNonEmptyStacksInRange(slotRange)
        .stream()
        .filter((ref) -> !ref.stack().isEmpty())
        .sorted(Comparator.comparing(ItemStackRef::stack, comparator))
        .collect(Collectors.toCollection(StacksList::new));

    return slotRange.project(stacks.stream().map(ItemStackRef::originalSlot).toList(), -1);
  }

  public StacksList getNonEmptyStacksInRange(SlotRange slotRange) {
    StacksList stacks = new StacksList(slotRange.size());
    for (int slot : slotRange.getSlots()) {
      ItemStack stack = this.getItem(slot);
      if (stack.isEmpty()) {
        continue;
      }
      stacks.add(new ItemStackRef(stack.copy(), slot));
    }
    return stacks;
  }

  private void copyStacks() {
    this.clearContent();
    for (int i = 0; i < this.getContainerSize(); i++) {
      this.setItem(i, this.source.getItem(i).copy());
    }
  }

  public record ItemStackRef(ItemStack stack, int originalSlot) {
    public static ItemStackRef empty() {
      return new ItemStackRef(ItemStack.EMPTY, -1);
    }
  }

  public static class StacksList extends ArrayList<ItemStackRef> {
    public StacksList() {
      super();
    }

    public StacksList(int initialSize) {
      super(initialSize);
    }

    public ItemStackRef getOrEmpty(int index) {
      if (index >= this.size()) {
        return ItemStackRef.empty();
      }
      return this.get(index);
    }
  }
}
