![Inventory Management](https://i.imgur.com/wXZra91.png)

![](https://img.shields.io/badge/Loader-Fabric%20|%20Forge%20|%20NeoForge-313e51?style=for-the-badge)
![](https://img.shields.io/badge/MC-26.1--26.1.2%20|%201.21%20|%201.20%20|%201.19%20|%201.18.2-313e51?style=for-the-badge)
![](https://img.shields.io/badge/Side-Client%20+%20Server-313e51?style=for-the-badge)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/inventory-management?style=flat&logo=modrinth&color=00AF5C)](https://modrinth.com/mod/inventory-management)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1293402?style=flat&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/inventory-management)
[![GitHub Repo stars](https://img.shields.io/github/stars/Roundaround/mc-inventory-management?style=flat&logo=github)](https://github.com/Roundaround/mc-inventory-management)

[![Support me on Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/roundaround)

Sort and transfer items with the click of a button, and a whole lot more. Inventory Management adds buttons to your inventory and containers that let you sort, transfer, and automatically stack your items, plus a handful of quality-of-life tools for keeping your inventory exactly how you want it. Works in single player by installing on your client, and in multiplayer by installing on both your client AND the server.

**Features at a glance:**

- **Sort** any container or your inventory with one click, alphabetically or in creative-menu order.
- **Transfer all** to dump one inventory into another (place all or take all) in a single click.
- **Auto-stack** to top up the matching stacks already in a container without disturbing anything else.
- **Locked slots** you protect with `Ctrl/Cmd`+click, so sort, transfer, and stack all skip them.
- **Hotbar swapping** to hold a key and scroll a whole main-inventory row into your hotbar on the fly.
- **Item durability** alerts when gear runs low, plus optional auto-replace right before a tool breaks.
- **Smart item grouping** that keeps variant families (all wool colors, all planks, and so on) together when sorting.

Every feature can be turned on or off independently in the config, so you can run just the parts you want.

![](https://i.imgur.com/GadzOcM.png)

---

### Configuration

You can configure the behavior of the mod from the `inventorymanagement.toml` file within your config folder. If you have ModMenu installed, you can also access the configuration through the UI in ModMenu's mod list, where each option shows a description tooltip when you hover it.

**General**

`modEnabled`: `true|false` (default `true`) - Master switch for the mod. When off, no buttons are shown and no behavior runs.

**Hotbar Swapping** (under the `hotbarSwap` section)

`hotbarSwap.enableHotbarSwap`: `true|false` (default `true`) - Master toggle for hotbar swapping. When off, the **Hotbar swap (hold)** key binding does nothing (scrolling and number keys no longer swap a row in) and the swapped-row badge is hidden. See the Hotbar swapping section below.

`hotbarSwap.hotbarSwapNumberKeys`: `true|false` (default `true`) - While holding the **Hotbar swap (hold)** key binding, let number keys `1`-`3` select a main-inventory row to swap into the hotbar (and re-pressing the active row's number swaps it back). On by default; turn it off to keep number keys changing your selected hotbar slot even while the key is held. Scrolling with the key held always works regardless.

**Item Durability** (under the `durability` section)

`durability.durabilityAlertEnabled`: `true|false` (default `true`) - Show a low-durability action-bar alert (with an anvil "ping") when a tool or equipped item drops past a threshold. Purely client-side: it reads durability that's already synced to you, so it works on any server, even vanilla. See the Item durability section below.

`durability.durabilityAlertThreshold1`: `<Integer>` (default `10`) - First percent-of-max durability threshold (`0`-`99`) that triggers the alert, firing once as durability crosses it downward. Set to `0` to skip this threshold.

`durability.durabilityAlertThreshold2`: `<Integer>` (default `5`) - Second percent-of-max durability threshold (`0`-`99`) that triggers the alert, firing once as durability crosses it downward. Set to `0` to skip this threshold.

`durability.durabilityAlertAtOne`: `true|false` (default `true`) - Also fire the alert at exactly `1` durability point remaining, in addition to the percentage thresholds.

`durability.durabilityAlertSound`: `true|false` (default `true`) - Play the anvil ping sound with the alert. The action-bar message always shows regardless.

`durability.durabilityAutoReplace`: `true|false` (default `false`) - Right before a held or worn item breaks, swap in a matching replacement from your inventory. Client-driven: your client detects the imminent break and requests the swap, so the mod must be installed on the server in multiplayer (no settings are synced).

`durability.durabilityAutoReplaceSimilar`: `true|false` (default `false`) - Relax auto-replace matching from strict (same item + enchantments) to similar (same category, ignoring material/enchantments): any glider for a glider, any item for the same armor slot, any item sharing the broken tool's tool tag (`#minecraft:pickaxes`, `axes`, `shovels`, `hoes`, `swords`).

**Sorting/Transfering** (under the `sorting` section)

`sorting.showSort`: `true|false` (default `true`) - Whether to show the sort buttons.

`sorting.showTransfer`: `true|false` (default `true`) - Whether to show the transfer-all (place/take) buttons.

`sorting.showStack`: `true|false` (default `true`) - Whether to show the auto-stack buttons.

`sorting.sortMode`: `"alphabetical"|"creative"` (default `"alphabetical"`) - How items are ordered when sorting. `alphabetical` sorts by item name; `creative` uses the creative-menu ordering.

`sorting.containersFirst`: `true|false` (default `false`) - Place content-holding items (shulker boxes, bundles, etc.) before other items. Only applies in `alphabetical` mode.

`sorting.defaultPosition`: `"(<Integer>,<Integer>)"` - Customize a default for button position.

`sorting.screenPositions`: `{"ID": "(<Integer>,<Integer>)"}` - Customize button position on a per-screen basis. While this can be modified manually in the config file, the recommended way to modify this is through the configuration UI. See below!

`sorting.enableSlotLocking`: `true|false` (default `true`) - Master toggle for locking player-inventory slots. When off, `Ctrl/Cmd`+click no longer locks slots, no markers or tooltips are drawn, and locked slots are no longer skipped by sort, auto-stack, and transfer. Already-locked slots are remembered for when you turn it back on. See the Locked slots section below.

`sorting.lockedSlotDisplay`: `"shown"|"hidden"|"hotkey"` (default `"shown"`) - When to draw the marker (border + darkened background) on locked slots. `shown` always draws it; `hidden` never does; `hotkey` only draws it while the **Peek locked slots** key binding is held. Locking itself (`Ctrl/Cmd`+click) and the hover tooltip are unaffected.

`lockedPlayerSlots` / `sorting.serverLockedPlayerSlots`: (default empty) - The locked player main-inventory slot indices, remembered **per world**. In single-player they live in that save's own config file (`<save>/config/inventorymanagement.toml`) under `lockedPlayerSlots`, so they travel with the save; on a multiplayer server they live in your global config under `sorting.serverLockedPlayerSlots`, a table keyed by server address. Either way they're managed for you by `Ctrl/Cmd`+clicking slots in-game (no GUI control), but live in the config file if you need to inspect or clear them.

**Sorting Groups** (under the `grouping` section)

`grouping.itemGrouping`: `true|false` (default `true`) - Master toggle for variant grouping: cluster related families of items (all wool colors, all planks, and so on) together instead of sorting each variant purely by name. See the Item grouping section below.

`grouping.dynamicGroups`: `true|false` (default `true`) - Master switch for datapack-defined grouping families (see the Datapack group tags section below).

`grouping.disabledDynamicGroups`: `["<tag id>", ...]` (default empty) - Individually disabled datapack grouping families, by full tag id. Managed in the config file (no GUI control).

`grouping.<family>`: `true|false` (default `true`) - One toggle per built-in variant family; turn one off to sort that family by plain name instead of clustering it. Families: `wool`, `wool_carpets`, `beds`, `candles`, `banners`, `shulker_boxes`, `dyes`, `terracotta`, `glazed_terracotta`, `concrete`, `concrete_powder`, `stained_glass`, `stained_glass_pane`, `planks`, `wooden_slabs`, `wooden_stairs`, `wooden_doors`, `wooden_trapdoors`, `wooden_fences`, `fence_gates`, `signs`, `hanging_signs`, `leaves`, `saplings`, `boats`, `spawn_eggs`, `pottery_sherds`, `horse_armor`, `coral`.

### Modifying the button positions

In order to maintain compatibility with other mods, you can now adjust the positions of your inventory management buttons! To adjust the position for all buttons, check out the option in the config for `sorting.defaultPosition` or if you have ModMenu installed, in the config UI. If you have a particular screen that needs adjusted (maybe another mod added more inventory space, for example), you can hold `Ctrl/Cmd` and click on one of the UI buttons to open up the per-screen position editor! This editor will adjust the button positions for the current screen only, so you can tweak the positions all you want to get it aligned with your shiny backpack mod's UI! ;)

Once the editor is open, **just click and drag the buttons** with your mouse to slide them into place, and they follow the cursor one-to-one. Prefer precision? The arrow keys still nudge them a pixel at a time (hold `Shift` for bigger jumps), and `Ctrl/Cmd`+`R` resets to the default. The same drag-and-drop works in the `defaultPosition` editor in the config UI.

`Ctrl/Cmd`+click is the primary way in, but there are also dedicated key bindings to open the editor (one for the player inventory buttons, one for the container buttons). **All of Inventory Management's key bindings are unbound by default.** If you'd like to use them, assign keys in Options, then Controls, under the **Inventory Management** category.

---

## Sort

![](https://i.imgur.com/Vcy2WL1.png)

A sort button will now appear in all containers (that are large enough) and your inventory. Clicking this button will sort the inventory automatically! For the most part this sorting is alphabetical, with a few manually-entered exceptions. Sorting ignores your hotbar, so you don't have to worry about it messing up your main items!

### Before

![](https://i.imgur.com/jt2uAGJ.png)

### After

![](https://i.imgur.com/0nwOaRO.png)

## Transfer all

![](https://i.imgur.com/hM52cuQ.png)

The place/take all buttons will transfer one entire inventory into the other! It starts at the top-left slot and works its way through the items until the other inventory is full. Similar to sorting, this mechanism will ignore your hotbar (and equipped items).

## Automatically stack

![](https://i.imgur.com/mpt6Ycz.png)

Similar to the transfer all buttons, there will also be buttons for stacking into/from a container. When clicked, this button will take all the items in the source inventory and stack them into any non-full stacks in the other, transferring only items required to fill up the stacks!

### Before

![](https://i.imgur.com/xG5e1ZW.png)

### After

![](https://i.imgur.com/yXyvZO6.png)

## Locked slots

![](https://raw.githubusercontent.com/Roundaround/mc-inventory-management/refs/heads/main/assets/screenshots/locked_slots.png)

Got a slot you never want disturbed, like your spare blocks, food, or that one stack you keep in the same spot out of habit? **Lock it.** Hold `Ctrl/Cmd` and click any slot in your main inventory to toggle a lock on it. Locked slots are skipped by **sort**, **auto-stack**, and **transfer-all (place/take)**, so those operations will never move, reorder, or pull items out of a locked slot. Locking and unlocking is the same `Ctrl/Cmd`+click, and it works with either mouse button.

Only the main inventory grid is lockable. Your hotbar, armor, off-hand, and any open container's slots aren't (the hotbar is already ignored by every operation anyway). Locks are remembered **per world**: each single-player save and each multiplayer server keeps its own set (single-player locks even travel inside the save folder), and they persist across sessions. An empty locked slot stays reserved, so a transfer won't fill it.

By default a locked slot is marked with a subtle darkened background and a border drawn beneath the item, and hovering it shows a `Locked (Ctrl+click to unlock)` tooltip. Don't like the markers? The `lockedSlotDisplay` config option lets you set them to **Always shown** (default), **Hidden**, or **While hotkey held**. That last one only draws them while you hold the **Peek locked slots** key binding, keeping your inventory clean until you want to check what's locked. The hover tooltip always shows regardless, so a locked slot is never a mystery.

> **Tip:** like all of Inventory Management's key bindings, **Peek locked slots** is unbound by default. Assign it in Options, then Controls, under the **Inventory Management** category if you want to use the `hotkey` display mode.

---

## Hotbar swapping

![](https://raw.githubusercontent.com/Roundaround/mc-inventory-management/refs/heads/main/assets/screenshots/hotbar_swap.png)

Want a second (or third) bar of items within thumb's reach without opening your inventory? **Hold the swap key and scroll.** Each scroll cycles which of your three main-inventory rows is exchanged with your hotbar, stepping from normal to row 1 to row 2 to row 3 and back to normal, wrapping around. The items physically swap places, so whatever was on your hotbar lands in that row and vice versa. Only one row is ever displaced at a time, and scrolling back to normal puts everything home.

Prefer pressing a key over scrolling? With the `hotbarSwapNumberKeys` option on (the default), while you hold the swap key, number keys `1`-`3` jump straight to that row; pressing the active row's number again swaps it back to normal. While the swap key is held the number keys won't change your selected hotbar slot, so `4`-`9` are simply ignored. Turn the option off if you'd rather number keys always behave normally even with the swap key held.

A small marker shows which row is active: a colored tab to the left of the swapped row in any inventory/container screen, and an unobtrusive badge with the row number (`1`-`3`) just left of the hotbar on the in-game HUD.

Behind the scenes, each swap exchanges a row's nine slots with the hotbar's nine, slot for slot, where row 1 is the top of the main grid and row 3 is the row nearest the hotbar. The movement happens server-side through a packet, the same way the sort button works, and switching rows always restores the previous one home before applying the new one. The server trusts the client's view of which row is currently out, so if the hotbar's contents change while a row is swapped in (an item breaks, a pickup lands there, a `/give`, another mod), the next swap can move the wrong slots. Scroll back to normal before doing anything that reshuffles a displaced hotbar.

If you ever get out of sync, for example after quitting the game mid-swap, press the **Reset hotbar swap** key binding. It clears the mod's tracking back to "normal" **without moving any items**, re-baselining to whatever is physically on your hotbar right now.

> **Note:** because the items physically swap places (and the server saves them), quitting while a row is swapped in leaves those items displaced when you log back in, even though the indicator shows "normal". To put everything home, re-select that row (scroll or number-key it back in) and then scroll back to normal.

> **Tip:** like all of Inventory Management's key bindings, both **Hotbar swap (hold)** and **Reset hotbar swap** are unbound by default. Assign them in Options, then Controls, under the **Inventory Management** category to use this feature.

---

## Item durability

![](https://raw.githubusercontent.com/Roundaround/mc-inventory-management/refs/heads/main/assets/screenshots/durability.png)

Tired of losing a hard-won tool because you didn't notice it was about to break? The **Item Durability** options give you two independent ways to look after your gear. Each is toggled separately in the config; the alert is on by default and auto-replace is off by default.

**Low-durability alert.** When a tool or equipped item (including an elytra) drops past a threshold, you get an action-bar message, *"\<item\> durability low! \<remaining\> of \<max\> remaining"*, along with an anvil "ping". It watches your main hand, off-hand, and all four armor slots, and fires once each time an item crosses a threshold on the way down, in the spirit of the Vanilla Tweaks "Durability Ping" datapack. There are two configurable percentage thresholds (default **10%** and **5%**), plus a separate **alert at 1 remaining** option for that final point. It's purely client-side: it reads durability that's already synced to you, so it works on **any** server, even vanilla. Turn off the sound with `durabilityAlertSound` if you only want the message, or adjust the two threshold sliders in the config (set either to `0` to skip it).

**Auto-replace before break.** Right before a held or worn item would break, the mod swaps in a matching replacement from your inventory. The worn-out item lands back in the slot the replacement came from, so you barely skip a beat and never lose the old one mid-action. By default the match is **strict** (same item, same enchantments), but `durabilityAutoReplaceSimilar` relaxes it to **similar**: any glider for a glider, any piece for the same armor slot, or any tool sharing the broken one's category (pickaxe, axe, shovel, hoe, sword). It scans your main inventory and off-hand and picks the candidate with the most remaining durability. Because it's only ever a two-slot swap, it can never duplicate or lose items.

One thing to know: auto-replace kicks in at an item's very last durability point, so an action that burns more than one point at once can still break it before the swap happens. The alert, and a quick manual swap, still have your back there.

**Multiplayer.** Auto-replace is **client-driven**: your client, which already sees your gear's durability, notices an item is about to break, picks the replacement, and asks the server to perform the swap, exactly how the sort and transfer buttons work. Nothing about your settings is synced, and the request carries everything the server needs. The mod must be installed **on the server** in multiplayer for the swap to happen, where the server validates and applies it. The alert is purely client-side and needs nothing on the server.

All durability options live under the `durability` section of the config.

---

## Item grouping

![](https://raw.githubusercontent.com/Roundaround/mc-inventory-management/refs/heads/main/assets/screenshots/item_grouping.png)

When `itemGrouping` is on (it is by default), the **alphabetical** sort clusters families of related
items together instead of scattering each variant by its own name, so all 16 wool colors land
together, all plank types land together, and so on. Each built-in family has its own
`grouping.<id>` toggle (all default on); turn one off to fall back to plain name sorting for just
that family.

A family "lands" in the sorted result at the display-name slot of its **anchor** (for the color
families that's the white variant, e.g. the whole wool cluster sorts where *White Wool* would).
Built-in families are listed in the Configuration section above.

Groups are consulted on a **first-match-wins** basis in this order: **built-ins, then mod-registered
groups, then datapack groups**. Built-ins are always consulted first, so nothing a mod or datapack
adds can hijack a vanilla family.

---

## For mod & pack developers

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
