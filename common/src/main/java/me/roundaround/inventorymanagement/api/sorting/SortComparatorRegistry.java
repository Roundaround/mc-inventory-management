package me.roundaround.inventorymanagement.api.sorting;

import me.roundaround.inventorymanagement.inventory.sorting.ConditionalComparator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A process-global registry of raw {@link Comparator}{@code <}{@link ItemStack}{@code >}
 * contributions that other mods can add to the inventory sort.
 *
 * <p>This is a <strong>tie-break extension point</strong>, distinct from the grouping registry
 * ({@link ItemVariantRegistry#registerModGroup(VariantGroup)}). Group contributions <em>cluster</em>
 * related items so they sort as a block; comparator contributions <em>refine the order</em> among
 * stacks the rest of the chain already considers equal. The registry occupies a single tie-break
 * slot inside the per-sort {@code ItemStackComparator} chain, consulted only <em>after</em> the
 * user's primary order (alphabetical/creative) and every built-in metadata comparator have all
 * returned {@code 0} for a pair. Consequently a contribution can <strong>never</strong> reorder
 * items the primary or metadata keys already distinguish &mdash; it only votes among
 * otherwise-identical-looking stacks. This is a hard guarantee, by construction of the chain.
 *
 * <p><strong>Ordering.</strong> Contributions are consulted in ascending {@code priority} order:
 * <em>lower number = consulted first = wins ties earlier</em>. The registry behaves first-non-zero-
 * wins, so the lowest-priority-number contribution that returns a non-zero result for a given pair
 * decides their relative order and short-circuits the rest; a contribution that returns {@code 0}
 * (for example a {@link #registerKey key contribution} whose predicate does not accept both stacks)
 * falls through to the next contribution. Among contributions of <em>equal</em> priority,
 * registration order is the tie-break (the first registrant is consulted first and therefore wins an
 * overlap); this is guaranteed by a stable sort over an insertion-ordered backing list reinforced by
 * an explicit monotonic sequence number, so sorts are fully reproducible.
 *
 * <p><strong>Timing.</strong> Register from your mod-init entrypoint, <em>before</em> the first
 * inventory sort. The registry itself is read live &mdash; each sort builds a fresh
 * {@code ItemStackComparator}, and the registry-backed slot re-reads {@link #comparators()} on every
 * comparison, so a contribution added after the game has started is picked up by the next sort. But
 * the surrounding chain caches: the name and metadata comparators cache their per-stack key mapping,
 * so registering <em>mid-sort</em> is unsupported. Register before the first sort to keep behavior
 * deterministic.
 *
 * <p><strong>Identity.</strong> {@code id} is a label only, for diagnostics and future tooling. v1
 * does not dedupe or replace by id: re-registering the same id appends an additional contribution.
 *
 * <p><strong>Enablement.</strong> Contributions are always-on. To expose a user toggle, gate the
 * behavior inside your own {@link Predicate predicate} or {@link Comparator} using your mod's config
 * &mdash; Inventory Management does not create a config option or GUI section for them.
 *
 * <p><strong>Thread-safety.</strong> Mutators are {@code synchronized} on this class and invalidate
 * a cached, priority-sorted {@link List#copyOf immutable} snapshot; readers serve that snapshot via a
 * lock-free {@code volatile} read, rebuilding under the lock only when a registration has marked it
 * dirty. Because the snapshot is immutable, an in-flight sort iterating it can never observe a torn
 * list while a concurrent registration mutates the backing list &mdash; it keeps using the snapshot
 * it already holds.
 */
public final class SortComparatorRegistry {
  private SortComparatorRegistry() {
  }

  /** Insertion-ordered backing list. Guarded by the class monitor; never exposed directly. */
  private static final List<Entry> CONTRIBUTIONS = new ArrayList<>();

  /** Monotonic insertion counter, the explicit stable tie-break for equal priorities. */
  private static int seq = 0;

  /**
   * Cached priority-sorted snapshots, lazily (re)built by {@link #rebuildCaches()}. A {@code null}
   * value is the dirty flag: {@link #register} and {@link #clearForTest} null both fields (under the
   * class monitor) to invalidate, and the next read rebuilds. {@code volatile} so the per-comparison
   * reader observes publication without taking the lock &mdash; the common, registry-stable case is a
   * single volatile read with no allocation or sort.
   */
  private static volatile List<Contribution> cachedList;
  private static volatile List<Comparator<ItemStack>> cachedComparators;

  /** Internal entry: a contribution plus its insertion sequence number. */
  private record Entry(Identifier id, int priority, Comparator<ItemStack> comparator, int seq) {
  }

  /**
   * A registered comparator contribution, as surfaced by {@link #list()}.
   *
   * @param id         the diagnostic label this contribution was registered under
   * @param priority   lower = consulted first; see the class-level Ordering note
   * @param comparator the contributed comparator
   */
  public record Contribution(Identifier id, int priority, Comparator<ItemStack> comparator) {
  }

  /**
   * Register a raw comparator contribution (advanced path). The comparator is consulted in the
   * tie-break slot and <strong>must return {@code 0} for any pair it does not recognize</strong> so
   * that lower-priority contributions can vote &mdash; you own that contract. For the safe path that
   * builds the recognition guard for you, prefer {@link #registerKey}.
   *
   * @param id         a diagnostic label (not deduped &mdash; re-registering an id appends)
   * @param priority   lower number = consulted first = wins ties earlier
   * @param comparator the comparator to consult; must return {@code 0} for unrecognized pairs
   */
  public static synchronized void register(Identifier id, int priority, Comparator<ItemStack> comparator) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(comparator, "comparator");
    CONTRIBUTIONS.add(new Entry(id, priority, comparator, seq++));
    invalidate();
  }

  /**
   * Register a key-based comparator contribution (safe path). Builds a comparator that orders stacks
   * by {@code key} but <em>only</em> when {@code applies} accepts <strong>both</strong> operands;
   * for any pair where the predicate rejects either stack it returns {@code 0} and falls through to
   * the next contribution. This is the recommended entry point: it cannot accidentally reorder items
   * your mod does not recognize.
   *
   * @param id       a diagnostic label (not deduped &mdash; re-registering an id appends)
   * @param priority lower number = consulted first = wins ties earlier
   * @param applies  the predicate gating which stacks this contribution recognizes
   * @param key      the comparable sort key extracted from a recognized stack
   * @param <K>      the key type
   */
  public static synchronized <K extends Comparable<? super K>> void registerKey(
      Identifier id, int priority, Predicate<ItemStack> applies, Function<ItemStack, K> key) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(applies, "applies");
    Objects.requireNonNull(key, "key");
    register(id, priority, ConditionalComparator.of(applies, Comparator.comparing(key)));
  }

  /**
   * A priority-ordered, immutable snapshot of the registered contributions. Sorted ascending by
   * {@code priority}, with registration order as the tie-break for equal priorities. The cached
   * snapshot is reused until the next {@link #register}/{@link #clearForTest} invalidates it, so
   * repeated reads neither allocate nor re-sort. The returned list is immutable; mutating the
   * registry afterward does not affect a snapshot already handed out.
   */
  public static List<Contribution> list() {
    List<Contribution> snapshot = cachedList;
    if (snapshot != null) {
      return snapshot;
    }
    rebuildCaches();
    return cachedList;
  }

  /**
   * A priority-ordered, immutable snapshot of just the contributed comparators. This is the hot path:
   * the registry-backed tie-break slot calls it on every comparison, so the result is a cached
   * snapshot served by a single {@code volatile} read in the common case (rebuilt only after a
   * registration invalidates the cache). Equivalent to mapping {@link #list()} to
   * {@link Contribution#comparator()}.
   */
  public static List<Comparator<ItemStack>> comparators() {
    List<Comparator<ItemStack>> snapshot = cachedComparators;
    if (snapshot != null) {
      return snapshot;
    }
    rebuildCaches();
    return cachedComparators;
  }

  /**
   * Sort the backing list once and publish both immutable cache snapshots. Synchronized; the
   * double-check skips the work if another thread rebuilt while this one waited for the lock. Both
   * caches are always published together, so a reader never sees one fresh and the other dirty.
   */
  private static synchronized void rebuildCaches() {
    if (cachedList != null && cachedComparators != null) {
      return;
    }
    List<Entry> sorted = new ArrayList<>(CONTRIBUTIONS);
    sorted.sort(Comparator.comparingInt(Entry::priority).thenComparingInt(Entry::seq));
    List<Contribution> contributions = new ArrayList<>(sorted.size());
    List<Comparator<ItemStack>> comparators = new ArrayList<>(sorted.size());
    for (Entry e : sorted) {
      contributions.add(new Contribution(e.id(), e.priority(), e.comparator()));
      comparators.add(e.comparator());
    }
    cachedList = List.copyOf(contributions);
    cachedComparators = List.copyOf(comparators);
  }

  /** Mark both cache snapshots dirty. Called from the synchronized mutators while holding the lock. */
  private static void invalidate() {
    cachedList = null;
    cachedComparators = null;
  }

  /** Test-only: empty the global registry so tests don't leak contributions into each other. */
  static synchronized void clearForTest() {
    CONTRIBUTIONS.clear();
    seq = 0;
    invalidate();
  }
}
