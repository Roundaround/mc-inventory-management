package me.roundaround.inventorymanagement.api.sorting;

import me.roundaround.inventorymanagement.generated.Constants;
import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * A named registry of {@link VariantGroup item-variant families} consulted by the inventory-sort
 * comparator.
 *
 * <p>Two instances are meaningful to the sort:
 * <ul>
 *   <li>{@link #COLOR} &mdash; the curated set of built-in families populated by
 *       {@link GroupBootstrap} in the load-bearing {@link GroupDefs#ALL} order.</li>
 *   <li>{@link #DYNAMIC} &mdash; the data-driven families discovered from synced item tags under
 *       the {@code grouping/} folder convention (see {@link DynamicTagGroups}). Rebuilt atomically
 *       on every tag reload; never touched by {@link GroupBootstrap}.</li>
 * </ul>
 *
 * <p>Mod-registered groups live in a separate static {@link #MOD_GROUPS} list (see
 * {@link #registerModGroup(VariantGroup)}), not in any named registry instance, so the curated
 * built-in set stays clean.
 *
 * <p>The comparator iterates {@link #effectiveGroups()} &mdash; built-ins, then mod groups, then
 * dynamic groups &mdash; with first-match-wins semantics.
 */
public class ItemVariantRegistry {
  private static final HashMap<Identifier, ItemVariantRegistry> REGISTRIES = new HashMap<>();

  /** Curated built-in families, populated by {@link GroupBootstrap} from {@link GroupDefs#ALL}. */
  public static final ItemVariantRegistry COLOR = register("color");

  /**
   * Data-driven families discovered from synced item tags. Swapped wholesale by
   * {@link DynamicTagGroups#rebuild} on tag reload via {@link #replaceAll(Collection)}; never
   * mutated by {@link GroupBootstrap}, so the built-ins are unaffected by reloads.
   */
  public static final ItemVariantRegistry DYNAMIC = register("dynamic");

  /**
   * Code-registered families contributed by other mods through
   * {@link #registerModGroup(VariantGroup)}. Append-only, consulted strictly after every
   * {@link #COLOR} built-in.
   */
  private static final List<VariantGroup> MOD_GROUPS = new ArrayList<>();

  private final Identifier id;

  /**
   * Backing list reference. Read through {@link #list()} and swapped atomically by
   * {@link #replaceAll(Collection)} so a concurrent sort never observes a torn list.
   */
  private volatile List<VariantGroup> groups = List.of();

  private ItemVariantRegistry(Identifier id) {
    this.id = id;
  }

  public Identifier getId() {
    return this.id;
  }

  /**
   * Append a single group to this instance. Used by {@link GroupBootstrap} to build up
   * {@link #COLOR}. Not for mod consumers &mdash; use {@link #registerModGroup(VariantGroup)} so
   * your group is appended after the curated built-ins instead of interleaved with them.
   */
  public synchronized void register(VariantGroup group) {
    ArrayList<VariantGroup> copy = new ArrayList<>(this.groups);
    copy.add(group);
    this.groups = Collections.unmodifiableList(copy);
  }

  /**
   * Atomically replace the entire backing list. Used by {@link DynamicTagGroups#rebuild} on tag
   * reload: a single volatile-reference swap means an in-flight {@link #list()} reader either sees
   * the whole old list or the whole new one, never a partial state.
   */
  public synchronized void replaceAll(Collection<VariantGroup> groups) {
    this.groups = List.copyOf(groups);
  }

  /** Unmodifiable, point-in-time view of this instance's groups. */
  public List<VariantGroup> list() {
    return this.groups;
  }

  private static ItemVariantRegistry register(String id) {
    return register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, id));
  }

  /**
   * Create and register a named registry instance.
   *
   * <p><strong>Not consulted by the sort.</strong> Only {@link #COLOR}, {@link #MOD_GROUPS}, and
   * {@link #DYNAMIC} feed {@link #effectiveGroups()}; a registry created here is reachable via
   * {@link #get(Identifier)} but the inventory comparator never reads it. Mods wanting their groups
   * to actually affect sorting must call {@link #registerModGroup(VariantGroup)} instead of minting
   * a private registry.
   */
  public static ItemVariantRegistry register(Identifier id) {
    return register(id, List.of());
  }

  /**
   * Create and register a named registry instance seeded with {@code initialEntries}.
   *
   * <p><strong>Not consulted by the sort</strong> &mdash; see {@link #register(Identifier)}.
   */
  public static ItemVariantRegistry register(Identifier id, Collection<VariantGroup> initialEntries) {
    ItemVariantRegistry registry = new ItemVariantRegistry(id);
    registry.groups = List.copyOf(initialEntries);

    REGISTRIES.put(id, registry);

    return registry;
  }

  public static ItemVariantRegistry get(Identifier id) {
    return REGISTRIES.get(id);
  }

  /**
   * Register a variant family contributed by another mod. The group is appended to a dedicated
   * mod-group list that the inventory-sort comparator consults <em>after</em> every built-in
   * {@link #COLOR} family and <em>before</em> the data-driven {@link #DYNAMIC} families.
   *
   * <p><strong>Ordering.</strong> Because the comparator is first-match-wins and built-ins are
   * consulted first, a mod group can only ever claim stacks that no built-in already claimed; it can
   * never shadow the load-bearing built-in order (e.g. {@code glazed_terracotta} before
   * {@code terracotta}). Among mods, registration order is the tiebreak (the first registrant wins
   * an overlap); ordering between unrelated mods is otherwise undefined &mdash; register a narrow
   * predicate to avoid surprises.
   *
   * <p><strong>Timing.</strong> Call this from your own mod-init entrypoint. On all three loaders,
   * dependent mods initialize after this mod's {@link GroupBootstrap#init()}, so built-ins-first
   * falls out for free. Register before the first inventory sort: {@code ItemNameComparator} extends
   * a caching comparator and caches its per-stack key mapping, so groups added after a comparator
   * instance has already mapped a given stack are not guaranteed to be picked up by that instance.
   *
   * <p><strong>Enablement.</strong> Mod groups are always-on by default. To expose a user toggle,
   * back your group's {@link VariantGroup#enabled()} supplier with your own mod's config and pass it
   * into one of the {@link VariantGroup} factories &mdash; this mod does not auto-generate a config
   * option or GUI section for mod-registered groups in v1.
   *
   * @param group the family to consult; build it with the {@link VariantGroup} factories
   */
  public static synchronized void registerModGroup(VariantGroup group) {
    MOD_GROUPS.add(group);
  }

  /** Unmodifiable, point-in-time view of the mod-registered groups, in registration order. */
  public static synchronized List<VariantGroup> modGroups() {
    return List.copyOf(MOD_GROUPS);
  }

  /** Test-only: empty the global mod-group list so tests don't leak registrations into each other. */
  static synchronized void clearModGroupsForTest() {
    MOD_GROUPS.clear();
  }

  /**
   * The full ordered group sequence the inventory-sort comparator consults, first-match-wins:
   * {@link #COLOR} built-ins, then {@link #modGroups()} mod groups, then {@link #DYNAMIC} data-driven
   * groups. Built-ins first guarantees no mod or pack tag can hijack a vanilla family.
   */
  public static List<VariantGroup> effectiveGroups() {
    List<VariantGroup> builtins = COLOR.list();
    List<VariantGroup> mods = modGroups();
    List<VariantGroup> dynamic = DYNAMIC.list();

    ArrayList<VariantGroup> effective = new ArrayList<>(builtins.size() + mods.size() + dynamic.size());
    effective.addAll(builtins);
    effective.addAll(mods);
    effective.addAll(dynamic);
    return Collections.unmodifiableList(effective);
  }
}
