package me.roundaround.inventorymanagement.api.sorting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Single source of truth for every item-grouping variant family. The same ordered list drives both
 * config-option registration ({@code InventoryManagementConfig}) and registry bootstrap
 * ({@code GroupBootstrap}), so toggle creation and group construction can never drift out of order.
 *
 * <p>List order is load-bearing: {@code ItemNameComparator} consults the registry first-match-wins,
 * so more-specific predicate families must precede the broader families they could otherwise
 * shadow (glazed_terracotta before terracotta, stained_glass_pane before stained_glass,
 * concrete_powder before concrete). The remaining families are mutually disjoint and
 * order-independent.
 */
public final class GroupDefs {
  private GroupDefs() {
  }

  /**
   * A grouping family. {@code factory} takes the per-group enabled supplier and returns the fully
   * built {@link VariantGroup}, encapsulating whether the family is tag- or predicate-based.
   * {@code displayName} feeds the generated config comment; the GUI label and section header come
   * from the lang file keyed on {@code id}.
   */
  public record GroupDef(String id, String displayName, Function<BooleanSupplier, VariantGroup> factory) {
  }

  /**
   * Predicate matching a stack whose registry id path ends with {@code suffix}.
   */
  private static Predicate<ItemStack> idEndsWith(String suffix) {
    return (stack) -> idPath(stack).endsWith(suffix);
  }

  /**
   * Predicate matching a stack whose registry id path contains {@code fragment}.
   */
  private static Predicate<ItemStack> idContains(String fragment) {
    return (stack) -> idPath(stack).contains(fragment);
  }

  private static String idPath(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
  }

  /**
   * Description id of a representative item, used as a predicate family's cluster anchor so the
   * whole family lands at that item's display-name slot.
   */
  private static String descId(Item item) {
    return item.getDescriptionId();
  }

  public static final List<GroupDef> ALL = List.of(
      // --- Predicate families that must precede the broader tag/predicate families they could shadow ---
      new GroupDef("glazed_terracotta", "Glazed Terracotta",
          (enabled) -> VariantGroup.byPredicate(
              idEndsWith("_glazed_terracotta"), descId(Items.GLAZED_TERRACOTTA.white()), enabled)),
      new GroupDef("terracotta", "Terracotta",
          (enabled) -> VariantGroup.by(Items.TERRACOTTA, ItemTags.TERRACOTTA, enabled)),

      new GroupDef("stained_glass_pane", "Stained Glass Panes",
          (enabled) -> VariantGroup.byPredicate(
              idEndsWith("_stained_glass_pane"), descId(Items.STAINED_GLASS_PANE.white()), enabled)),
      new GroupDef("stained_glass", "Stained Glass",
          (enabled) -> VariantGroup.byPredicate(
              idEndsWith("_stained_glass"), descId(Items.STAINED_GLASS.white()), enabled)),

      new GroupDef("concrete_powder", "Concrete Powder",
          (enabled) -> VariantGroup.byPredicate(
              idEndsWith("_concrete_powder"), descId(Items.CONCRETE_POWDER.white()), enabled)),
      new GroupDef("concrete", "Concrete",
          (enabled) -> VariantGroup.byPredicate(
              idEndsWith("_concrete"), descId(Items.CONCRETE.white()), enabled)),

      // --- Tag-based color/dye families (mutually disjoint, order-independent) ---
      new GroupDef("wool", "Wool",
          (enabled) -> VariantGroup.by(Items.WOOL.white(), ItemTags.WOOL, enabled)),
      new GroupDef("wool_carpets", "Carpets",
          (enabled) -> VariantGroup.by(Items.CARPET.white(), ItemTags.WOOL_CARPETS, enabled)),
      new GroupDef("beds", "Beds",
          (enabled) -> VariantGroup.by(Items.BED.white(), ItemTags.BEDS, enabled)),
      new GroupDef("candles", "Candles",
          (enabled) -> VariantGroup.by(Items.CANDLE, ItemTags.CANDLES, enabled)),
      new GroupDef("banners", "Banners",
          (enabled) -> VariantGroup.by(Items.BANNER.white(), ItemTags.BANNERS, enabled)),
      new GroupDef("shulker_boxes", "Shulker Boxes",
          (enabled) -> VariantGroup.by(Items.SHULKER_BOX, ItemTags.SHULKER_BOXES, enabled)),
      new GroupDef("dyes", "Dyes",
          (enabled) -> VariantGroup.by(Items.DYE.white(), ItemTags.DYES, enabled)),

      // --- Tag-based wood/material families (mutually disjoint, order-independent) ---
      new GroupDef("planks", "Planks",
          (enabled) -> VariantGroup.by(Items.OAK_PLANKS, ItemTags.PLANKS, enabled)),
      new GroupDef("wooden_slabs", "Wooden Slabs",
          (enabled) -> VariantGroup.by(Items.OAK_SLAB, ItemTags.WOODEN_SLABS, enabled)),
      new GroupDef("wooden_stairs", "Wooden Stairs",
          (enabled) -> VariantGroup.by(Items.OAK_STAIRS, ItemTags.WOODEN_STAIRS, enabled)),
      new GroupDef("wooden_doors", "Wooden Doors",
          (enabled) -> VariantGroup.by(Items.OAK_DOOR, ItemTags.WOODEN_DOORS, enabled)),
      new GroupDef("wooden_trapdoors", "Wooden Trapdoors",
          (enabled) -> VariantGroup.by(Items.OAK_TRAPDOOR, ItemTags.WOODEN_TRAPDOORS, enabled)),
      new GroupDef("wooden_fences", "Wooden Fences",
          (enabled) -> VariantGroup.by(Items.OAK_FENCE, ItemTags.WOODEN_FENCES, enabled)),
      new GroupDef("fence_gates", "Fence Gates",
          (enabled) -> VariantGroup.by(Items.OAK_FENCE_GATE, ItemTags.FENCE_GATES, enabled)),
      new GroupDef("signs", "Signs",
          (enabled) -> VariantGroup.by(Items.OAK_SIGN, ItemTags.SIGNS, enabled)),
      new GroupDef("hanging_signs", "Hanging Signs",
          (enabled) -> VariantGroup.by(Items.OAK_HANGING_SIGN, ItemTags.HANGING_SIGNS, enabled)),
      new GroupDef("leaves", "Leaves",
          (enabled) -> VariantGroup.by(Items.OAK_LEAVES, ItemTags.LEAVES, enabled)),
      new GroupDef("saplings", "Saplings",
          (enabled) -> VariantGroup.by(Items.OAK_SAPLING, ItemTags.SAPLINGS, enabled)),
      new GroupDef("boats", "Boats",
          (enabled) -> VariantGroup.by(Items.OAK_BOAT, ItemTags.BOATS, enabled)),

      // --- Predicate families with no vanilla item tag (disjoint, order-independent) ---
      new GroupDef("spawn_eggs", "Spawn Eggs",
          (enabled) -> VariantGroup.byPredicate(idEndsWith("_spawn_egg"), descId(Items.DYE.white()), enabled)),
      new GroupDef("pottery_sherds", "Pottery Sherds",
          (enabled) -> VariantGroup.byPredicate(idEndsWith("_pottery_sherd"), descId(Items.BRICK), enabled)),
      new GroupDef("horse_armor", "Horse Armor",
          (enabled) -> VariantGroup.byPredicate(idEndsWith("_horse_armor"), descId(Items.SADDLE), enabled)),
      new GroupDef("coral", "Coral",
          (enabled) -> VariantGroup.byPredicate(idContains("coral"), descId(Items.TUBE_CORAL), enabled))
  );
}
