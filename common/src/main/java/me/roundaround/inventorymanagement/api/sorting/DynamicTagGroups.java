package me.roundaround.inventorymanagement.api.sorting;

import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds data-driven {@link VariantGroup variant families} from synced item tags, with no code
 * required of pack authors.
 *
 * <p><strong>Convention.</strong> Any item tag whose id path starts with {@code grouping/}
 * &mdash; i.e. {@code #<namespace>:grouping/<name>}, file
 * {@code data/<ns>/tags/item/grouping/<name>.json} &mdash; becomes one variant family. The family's
 * predicate is {@code stack.is(tag)} (which reads the live, client-synced tag membership), and its
 * cluster anchor is the tag's language key (e.g. {@code tag.item.mymod.grouping.gems}). The
 * language-key anchor is reload-stable: it does not move when tag membership changes, unlike an
 * alphabetically-first-member anchor.
 *
 * <p><strong>Lifecycle.</strong> {@link #rebuild(HolderLookup.Provider)} is invoked from each
 * loader's native client tag-sync event (Fabric {@code CommonLifecycleEvents.TAGS_LOADED}, NeoForge
 * and Forge {@code TagsUpdatedEvent}). It enumerates the {@code grouping/} tags, sorts them by full
 * tag id for reproducible cluster assignment, builds one {@link VariantGroup} per tag, and swaps the
 * result into {@link ItemVariantRegistry#DYNAMIC} atomically. The built-in
 * {@link ItemVariantRegistry#COLOR} families and the mod groups are never touched.
 *
 * <p><strong>Caching caveat.</strong> {@code ItemNameComparator} caches its per-stack key mapping.
 * Comparator instances are constructed per sort invocation in practice, so a fresh sort (which a
 * {@code /reload}-driven inventory re-open triggers) sees the new dynamic set. If a long-lived
 * comparator instance is ever cached, it must be invalidated alongside this rebuild.
 *
 * <p>Discovery/parse is a pure function over a {@link HolderLookup.Provider}; the only part that is
 * not unit-testable is the live loader event firing.
 */
public final class DynamicTagGroups {
  /** Tag-id path prefix that marks an item tag as a grouping family. */
  public static final String GROUPING_PREFIX = "grouping/";

  private DynamicTagGroups() {
  }

  /**
   * Discover, build, and atomically install the dynamic grouping families from the synced item tags
   * in {@code registries}. Both {@link net.minecraft.core.RegistryAccess} and a raw
   * {@link HolderLookup.Provider} are accepted (the former is a subtype of the latter), covering the
   * Fabric/Forge {@code RegistryAccess} and NeoForge {@code HolderLookup.Provider} event payloads.
   */
  public static void rebuild(HolderLookup.Provider registries) {
    ItemVariantRegistry.DYNAMIC.replaceAll(discover(registries));
  }

  /** Empty the dynamic set (no-tags / pre-world case). */
  public static void clear() {
    ItemVariantRegistry.DYNAMIC.replaceAll(List.of());
  }

  /**
   * Pure discovery: enumerate {@code grouping/}-prefixed item tags in {@code registries}, sorted by
   * full tag id, and build one always-resolvable {@link VariantGroup} per tag with the config-backed
   * dynamic enabled supplier. Exposed (package-private) for unit testing over a simulated tag set.
   */
  static List<VariantGroup> discover(HolderLookup.Provider registries) {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();

    List<TagKey<Item>> tags = new ArrayList<>();
    registries.lookupOrThrow(Registries.ITEM)
        .listTags()
        .map(HolderSet.Named::key)
        .filter((key) -> key.location().getPath().startsWith(GROUPING_PREFIX))
        .forEach(tags::add);

    // getTags()/listTags() iteration order is not guaranteed stable across reloads; sort by full
    // tag id so cluster assignment for overlapping pack tags is reproducible.
    tags.sort(Comparator.comparing((key) -> key.location().toString()));

    List<VariantGroup> groups = new ArrayList<>(tags.size());
    for (TagKey<Item> tag : tags) {
      String fullTagId = tag.location().toString();
      groups.add(VariantGroup.by(tag, config.groupEnabledDynamic(fullTagId)));
    }
    return groups;
  }
}
