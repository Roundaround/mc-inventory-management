package me.roundaround.inventorymanagement.api.sorting;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * A single variant family for item-grouping during sort. The {@link #predicate} decides whether a
 * stack belongs to this family, {@link #groupProducer} maps a member stack to its sort-key list
 * (first element is the cluster anchor, the rest order members within the cluster), and
 * {@link #enabled} gates the family at sort time so a per-group config toggle can switch it off
 * without re-registration.
 */
public record VariantGroup(
    Predicate<ItemStack> predicate,
    VariantGroupProducer groupProducer,
    BooleanSupplier enabled
) {
  /**
   * Convenience constructor defaulting {@link #enabled} to always-on, preserving any raw 2-arg
   * {@code new VariantGroup(...)} callers.
   */
  public VariantGroup(Predicate<ItemStack> predicate, VariantGroupProducer groupProducer) {
    this(predicate, groupProducer, () -> true);
  }

  public static VariantGroup by(Item root, TagKey<Item> tag) {
    return by(root, tag, () -> true);
  }

  public static VariantGroup by(Item root, TagKey<Item> tag, BooleanSupplier enabled) {
    return new VariantGroup((stack) -> stack.is(tag), groupUnderItem(root), enabled);
  }

  public static VariantGroup by(TagKey<Item> tag) {
    return by(tag, () -> true);
  }

  public static VariantGroup by(TagKey<Item> tag, BooleanSupplier enabled) {
    return new VariantGroup((stack) -> stack.is(tag), groupUnderName(tag.location().toLanguageKey()), enabled);
  }

  public static VariantGroup by(String root, TagKey<Item> tag) {
    return by(root, tag, () -> true);
  }

  public static VariantGroup by(String root, TagKey<Item> tag, BooleanSupplier enabled) {
    return new VariantGroup((stack) -> stack.is(tag), groupUnderName(root), enabled);
  }

  /**
   * Build a predicate-driven group whose cluster anchors at {@code anchorDescriptionId} (e.g. the
   * white variant's description id) so the whole family lands at that display-name slot, with
   * members ordered by their own description id after the anchor.
   */
  public static VariantGroup byPredicate(
      Predicate<ItemStack> predicate, String anchorDescriptionId, BooleanSupplier enabled) {
    return new VariantGroup(
        predicate,
        (context, stack) -> List.of(anchorDescriptionId, getDescriptionId(stack)),
        enabled
    );
  }

  private static VariantGroupProducer groupUnderItem(Item item) {
    return (context, stack) -> List.of(
        getDescriptionId(stack.transmuteCopy(item, stack.getCount())),
        getDescriptionId(stack.is(item) ? ItemStack.EMPTY : stack)
    );
  }

  private static VariantGroupProducer groupUnderName(String root) {
    return (context, stack) -> List.of(root, getDescriptionId(stack));
  }

  private static String getDescriptionId(ItemStack stack) {
    if (stack.isEmpty()) {
      return "";
    }
    return stack.getItem().getDescriptionId();
  }
}
