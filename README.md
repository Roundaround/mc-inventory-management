![Inventory Management](https://i.imgur.com/wXZra91.png)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/inventory-management?style=flat&logo=modrinth&color=00AF5C)](https://modrinth.com/mod/inventory-management)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1293402?style=flat&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/inventory-management)
[![GitHub Repo stars](https://img.shields.io/github/stars/Roundaround/mc-inventory-management?style=flat&logo=github)](https://github.com/Roundaround/mc-inventory-management)

[![Support me on Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/roundaround)

Sort, transfer, and auto-stack items with the click of a button — plus locked slots, hotbar swapping, durability alerts, and smart item grouping.

## For mod and pack developers

Inventory Management exposes a small, **loader-agnostic** API (package
`me.roundaround.inventorymanagement.api.sorting`, with no Fabric/NeoForge/Forge types, so one call works
on all three loaders) plus a datapack convention for contributing your own item **variant groups**
to the sort. A variant group clusters related items so they sort as a block.

### Group registry (for mods)

Call `ItemVariantRegistry.registerModGroup(VariantGroup)` from your mod's init entrypoint. Build the
group with the `VariantGroup` factories:

| Factory | Use when |
|---|---|
| `VariantGroup.by(tag)` | members are an item tag; anchor is the tag's language key |
| `VariantGroup.by(rootItem, tag)` | members are an item tag; anchor is a representative item |
| `VariantGroup.by(anchorKey, tag)` | members are an item tag; anchor is an explicit translation key |
| `VariantGroup.byPredicate(pred, anchorDescId, enabled)` | members match a predicate; anchor is a description id |

```java
import me.roundaround.inventorymanagement.api.sorting.ItemVariantRegistry;
import me.roundaround.inventorymanagement.api.sorting.VariantGroup;

// Group all your gem items together, landing them at Ruby's display-name slot.
ItemVariantRegistry.registerModGroup(
    VariantGroup.byPredicate(
        stack -> stack.is(MyItems.RUBY) || stack.is(MyItems.SAPPHIRE),
        MyItems.RUBY.getDescriptionId(),
        () -> true));

// Or, for an item tag anchored on a representative item:
ItemVariantRegistry.registerModGroup(VariantGroup.by(MyItems.RED_CRYSTAL, MyTags.CRYSTALS));
```

- **Anchor**: the first sort key a group produces; the whole cluster lands at that item/key's
  alphabetical slot, with members ordered within it.
- **Ordering**: mod groups are consulted *after* every built-in (so they can never shadow a vanilla
  family) and *before* datapack groups. Among mods, registration order breaks an overlap, so register
  a narrow predicate (or a disjoint tag) to avoid fighting another mod for the same items.
- **Timing**: register from your mod-init entrypoint, *before* the first inventory sort.
- **Enablement**: mod groups are always-on by default. To give users a toggle, back the `enabled`
  `BooleanSupplier` with your own mod's config and pass it into the factory; Inventory Management does
  **not** create a config option or GUI section for mod-registered groups.
- **Don't** create your own registry via `ItemVariantRegistry.register(Identifier)` expecting the sort
  to read it; only `registerModGroup(...)` is consulted.

### Comparator registry (for mods)

Contribute a raw `Comparator<ItemStack>` ordering to the sort. This is **distinct from the group
registry**: a group *clusters* related items; a comparator contribution *refines the order* among
items the sort already considers equal.

> **This is a tie-break.** The comparator registry is consulted only **after** the user's primary
> order (alphabetical/creative) and every built-in metadata comparator have all tied for a pair. A
> contribution can **never** reorder items the primary or metadata keys already distinguish; it only
> refines ordering among otherwise-identical-looking stacks. No priority value, however low, can
> override the user's chosen order.

Two entry points on `SortComparatorRegistry`:

| Method | Use when |
|---|---|
| `registerKey(id, priority, applies, key)` | **Safe path**: your comparator only affects stacks your `applies` predicate accepts (both operands must match); everything else returns `0` and falls through. |
| `register(id, priority, comparator)` | **Raw/advanced path**: a full `Comparator<ItemStack>`. You own the contract that it returns `0` for any pair it doesn't recognize. |

```java
import me.roundaround.inventorymanagement.api.sorting.SortComparatorRegistry;
import net.minecraft.resources.Identifier;

// Among otherwise-identical ammo stacks, order fuller stacks first.
SortComparatorRegistry.registerKey(
    Identifier.fromNamespaceAndPath("mymod", "ammo_by_fullness"),
    100,
    stack -> stack.is(MyItems.ARROW_QUIVER),
    stack -> -stack.getCount());   // negative => higher count sorts earlier
```

- **Priority**: *lower number = consulted first = wins ties earlier*. The registry is
  first-non-zero-wins, so the lowest-priority-number contribution that returns non-zero for a pair
  decides it. Equal priority breaks by registration order (first registrant wins).
- **Timing**: register from your mod-init entrypoint, *before* the first inventory sort. The
  registry is read live (each sort sees current registrations), but registering *mid-sort* is
  unsupported.
- **Identity**: `id` is a label only (diagnostics). Re-registering the same id **appends** another
  contribution; it does not replace.
- **Enablement**: contributions are always-on. To give users a toggle, gate it inside your own
  predicate/comparator with your mod's config; Inventory Management adds no GUI for them.

### Datapack group tags (no code)

Datapacks (or resourcepacks that ship data) can define grouping families with **no code**: any item
tag whose path starts with `grouping/` automatically becomes a family.

- File: `data/<namespace>/tags/item/grouping/<name>.json`
- Tag id: `#<namespace>:grouping/<name>`, for example `#mymod:grouping/gems`

```json
{
  "replace": false,
  "values": [
    "minecraft:diamond",
    "minecraft:emerald",
    "mymod:ruby"
  ]
}
```

- The cluster **anchors at the tag's language key** (e.g. `tag.item.mymod.grouping.gems`). Add that
  key to your pack's `en_us.json` to give the cluster a friendly landing name; otherwise the raw key
  is used. The anchor is reload-stable (it doesn't move when tag membership changes).
- **Reload-aware**: item tags are synced to the client, so the datapack family set is rebuilt on
  world join and on `/reload`.
- **Toggles**: the `grouping.dynamicGroups` config option (default on) gates *all* datapack
  families at once; `grouping.disabledDynamicGroups` (a list of tag ids in the config file) disables
  individual ones.

## Installing

Grab a build from [Modrinth](https://modrinth.com/mod/inventory-management) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/inventory-management). Fabric builds need [Fabric API](https://modrinth.com/mod/fabric-api).

## Building from source

```sh
./gradlew build
```

Dev runs are per loader: `:fabric:runClient`, `:neoforge:runClient`, `:forge:runClient`, and the `runServer` equivalents. Game tests run with `./gradlew :fabric:runClientGameTests` and `:fabric:runServerGameTests`.

The build is an [Allay](https://github.com/Roundaround/allay) consumer and bundles [Trove](https://github.com/Roundaround/trove).

Shared code lives in `common/` and is added to each loader subproject via `srcDir`.

## Contributing

Issues and pull requests are welcome at [the issue tracker](https://github.com/Roundaround/mc-inventory-management/issues).

- Branch from `main`, which tracks the newest supported Minecraft version. Older lines live on their own version-named branches.
- Keep loader-agnostic code in `common/`; only genuinely loader-specific glue belongs in a loader subproject.
- Run `./gradlew build` plus the Fabric game tests before opening a PR, and add a changelog entry under `changelogs/` named for the version you're targeting.

## License

[MIT](LICENSE)
