# Hotbar swapping — design & maintenance notes

This document captures the design of the **Hotbar swapping** feature and the spots a maintainer needs
to keep in mind. The user-facing description lives in [`README.md`](README.md#hotbar-swapping); this
file is the implementation contract.

## What it does

Hold the **Hotbar swap (hold)** key binding and scroll to cycle which main-inventory row is exchanged
with the hotbar. Optionally (config-gated) use number keys `1`-`3` to pick a row directly. A second
key binding clears tracking, and an indicator marks the swapped row both in the inventory screen and
on the in-game HUD. The actual item movement happens server-side via a network packet, mirroring how
sorting works.

## Row → slot mapping

Player inventory slot indices (vanilla `net.minecraft.world.entity.player.Inventory`):

| Region   | Slots   |
| -------- | ------- |
| Hotbar   | `0`-`8` |
| Row 1    | `9`-`17`  (top of the main grid)         |
| Row 2    | `18`-`26` (middle)                       |
| Row 3    | `27`-`35` (bottom, nearest the hotbar)   |

"Swapping row *r* in" exchanges row *r*'s nine slots with the hotbar's nine slots, slot-for-slot.
`base = 9 + (r - 1) * 9`.

## Client state

- A single in-memory `int swappedRow` in `{0, 1, 2, 3}` (`0` = none / normal layout), held in
  `client/HotbarSwapClient.java`.
- **Not persisted to disk.** Reset to `0` on disconnect (detected each client tick when
  `Minecraft.getInstance().player == null`).
- Scroll cycles `0 → 1 → 2 → 3 → 0` (4 states, wrapping) via `cycle(double wheel)`
  (`Math.floorMod`).
- Number keys (when `hotbarSwapNumberKeys` is on): `1`/`2`/`3` select rows 1/2/3; pressing the active
  row's number toggles back to `0`. Keys `4`-`9` are consumed (no slot change) while the modifier is
  held.

## Packet flow (mirrors sorting)

- Every transition to a new row sends `Networking.HotbarSwapC2S(previousRow, newRow)` (two
  `VAR_INT`s) via `ClientNetworking.sendHotbarSwap(...)`.
- The server handler calls `ServerInventoryHelper.swapHotbarRows(player, previousRow, newRow)`, which
  **restores `previousRow` to its home first**, then applies `newRow`. This guarantees only one row is
  ever displaced at a time, even across consecutive cycles.
- The whole-row swap **intentionally ignores the locked-slots feature** — it is a transient display
  convenience, not a sort/transfer operation. (Noted in a comment on `swapRowWithHotbar`.)
- No explicit broadcast: the open menu syncs on its tick, the same way `applySort` relies on.

## Reset = clear-tracking-only

The **Reset hotbar swap** key binding sets `swappedRow = 0` **without sending a packet and without
moving items**. It re-baselines the mod's tracking to whatever is physically on the hotbar right now —
the escape hatch for desync (e.g. after quitting mid-swap). It is drained in the client tick loop via
`KeyMapping.consumeClick()`.

## Correctness boundary — server trusts the client (and items persist)

The protocol is **stateless on the server**: the server keeps no authoritative record of which row is
currently displaced. `swapHotbarRows(player, previousRow, newRow)` restores the client-supplied
`previousRow` to its home before applying `newRow`, trusting that the hotbar still holds `previousRow`'s
items. Two consequences a maintainer must keep in mind:

- **Cross-session physical displacement.** The swap moves items server-side and those moves are saved to
  disk (the backing `Inventory` list is serialized; `ServerPlayer.tick → broadcastChanges` syncs the open
  menu). If a player quits with a row swapped in, on reconnect the client tick loop has reset `swappedRow`
  to `0` (player became `null`), so **no indicator shows**, yet the items are still physically displaced
  (row's items in the hotbar, hotbar's items in slots 9-35). `reset()` is a no-op in that state because
  `swappedRow` is already `0`. **Recovery:** re-select that row (scroll/number-key it back in) and then
  return to normal, which sends a clean restore. This is an accepted design tradeoff of "reset =
  clear-tracking-only, re-baseline to physical", not a bug — but it is the single largest real-world UX
  hazard of the feature.
- **No prev-match validation = corruption window.** If the hotbar contents change while a row is swapped
  in (item breaks, a pickup lands in the displaced hotbar, a creative give, another mod, a dropped/out-of-
  order packet), the next transition's restore moves the *wrong* nine slots, permanently scrambling items.
  Unlike `applySort` (which reconstructs from a client-computed permutation and bails via the
  `slotsWithItems` guard when the source set doesn't match), `swapHotbarRows` has **no consistency check**.
  The **Reset hotbar swap** key only re-baselines client tracking; **it cannot undo a server swap that
  already moved the wrong items.**

If tighter correctness is ever wanted, make the server authoritative: track the currently-swapped row per
player server-side (transient per-UUID state) and ignore the client-supplied `previousRow` so the restore
uses the server's own source of truth, eliminating the whole prev-mismatch class of desync. That is a
deliberate protocol change, out of scope for the current stateless design.

## Key bindings

Both `hotbarSwapModifier` and `hotbarSwapReset` are registered in
`client/InventoryManagementKeyMappings.java` and are **unbound by default**
(`InputConstants.UNKNOWN`), consistent with every other key binding in this mod. Players assign them
under Options → Controls in the **Inventory Management** category.

## Indicators

Both are gated on `config.modEnabled` AND `swappedRow != 0`:

- **Inventory/container screen** (`mixin/HotbarSwapIndicatorMixin.java`): a small colored tab drawn in
  the gutter just left of the leftmost slot of the swapped row, at the `HEAD` of
  `AbstractContainerScreen#extractSlots` where coordinates are slot-local (same setup as
  `SlotLockMixin`).
- **In-game HUD** (`client/gui/hud/HotbarSwapHud.java`): a small badge with the active row number
  (`1`-`3`) just left of the centered hotbar, registered as a real HUD layer via `Hud.register(...)`
  (drawing directly into `Gui.extractRenderState` is silently culled on MC 26.1). The HUD badge also
  respects the hide-GUI toggle (F1) — it returns early when `Minecraft.renderNames()` is `false` — so the
  full gating list is: `swappedRow != 0`, config initialized + `modEnabled`, a live player, and the GUI
  not hidden.

## The two `@WrapOperation` input mixins — MAINTAINER NOTE

Two mixins wrap a single vanilla `INVOKE` each, both client-only (`@MixinEnv(MixinEnv.Env.CLIENT)`):

1. `mixin/HotbarSwapScrollMixin.java` — `@Mixin(MouseHandler.class)`, wraps the single
   `ScrollWheelHandler.getNextScrollWheelSelection(DII)I` call in `onScroll`. When the modifier is
   active, consumes the scroll for row cycling and returns the current selection unchanged.
2. `mixin/HotbarSwapNumberKeyMixin.java` — `@Mixin(Minecraft.class)`, wraps the single
   `Inventory.setSelectedSlot(I)V` call in `handleKeybinds`. When the modifier is active and number
   keys are enabled, routes `1`-`3` to row selection and swallows `4`-`9`.

**NeoForge/Forge sometimes relocate a patched vanilla method body into a synthetic lambda** (this mod
already has a documented `Item.useOn` case). `onScroll` and `handleKeybinds` are client-input methods
and less likely to be relocated, but if a `@WrapOperation` cannot find its `INVOKE` target on a
loader the game throws **at launch** (the generated mixin config uses `defaultRequire=1`). The Gradle
build does **not** validate mixin application — only a game launch does.

**Therefore: launch each loader once (`fabric`, `neoforge`, `forge`) to confirm both input mixins
attach.** If a future MC update relocates either method body into a lambda, add the lambda name to the
`method` array (e.g. `method = {"onScroll", "lambda$onScroll$0"}`), matching the existing `useOn`
pattern.

As of **MC 26.1.2**, both `INVOKE` targets were confirmed **inline** (not lambda-relocated) in the
NeoForge and Forge patched sources as well as vanilla — `ScrollWheelHandler.getNextScrollWheelSelection`
sits inline in `MouseHandler.onScroll` and `Inventory.setSelectedSlot` sits inline in
`Minecraft.handleKeybinds` on all three loaders — so the plain `method =` names suffice today. Re-run the
launch-each-loader step after any MC bump as the runtime confirmation.

## Mixin discovery

All three new mixins live under `common/.../mixin/` and are auto-discovered by Allay and split into
client/common arrays by `@MixinEnv`. Do **not** create or edit any `*.mixins.json` (they are generated
under `build/`).
