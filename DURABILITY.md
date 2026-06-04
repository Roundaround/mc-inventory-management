# Item Durability

Technical reference for the **Item Durability** feature group. For the player-facing description see the
[README](README.md#item-durability).

The group adds two independently-toggleable features. Both are **client-driven** — there is no
client↔server preference sync and no durability mixin. Feature 1 is purely client-side; Feature 2 has the
client send a one-shot action request that the server validates and applies (the same pattern as the
sort/transfer/auto-stack buttons).

---

## The two features

### 1. Low-durability alert (client-side)

Shows an action-bar message and plays an anvil "ping" when a tool or equipped item drops past a
configured threshold. Modeled on the Vanilla Tweaks "Durability Ping" datapack.

- **Where:** `common/.../client/DurabilityClient.java`, a per-tick poller registered via
  `ClientLifecycle.onTick(...)`.
- **Slots polled:** `MAINHAND`, `OFFHAND`, `HEAD`, `CHEST`, `LEGS`, `FEET` (so elytra in the chest slot
  is covered).
- **Firing rule:** for each configured percent threshold `T`, fire once when `prevPercent > T` and
  `curPercent <= T` (a downward crossing). Plus, if `durabilityAlertAtOne` is on, fire when
  `remaining == 1` and the previous remaining was `> 1`.
- **Per-slot arming:** tracked by `(itemIdentity, previousRemaining, replaceRequested)`. The identity is
  the registry-singleton `Item` (`stack.getItem()`), NOT the `ItemStack` reference — the client replaces a
  slot's `ItemStack` object on every durability sync, so keying on the reference made every damage tick
  look like a swap and suppressed all alerts.
- **Output:** `LocalPlayer.sendOverlayMessage(Component)` (item name gold, the rest red) and
  `Level.playLocalSound(..., SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 2.0f, false)` when
  `durabilityAlertSound` is on.
- **Networking:** none. Works on any server, including vanilla.

### 2. Auto-replace before break (client-driven)

Right before a held/worn item would break, swap in a matching replacement from the player inventory,
sending the worn-out item back to the slot the replacement came from (a pure two-slot exchange).

- **Trigger (client):** the same per-tick poll in `DurabilityClient`. When a polled slot's item
  `isDamageableItem()` and `nextDamageWillBreak()` (at its last durability point) and `durabilityAutoReplace`
  is on, the client calls `DurabilityReplace.findReplacement(...)` over its own inventory and, if a match is
  found, sends one `DurabilityReplaceC2S(fromSlot, targetSlot, similar)` request. The `replaceRequested`
  per-slot flag ensures the request is sent at most once per about-to-break item (re-armed when the item is
  repaired/replaced or the slot's `Item` changes).
- **Matching** (`common/.../durability/DurabilityReplace.java`, shared by client decision + server
  validation):
  - *Strict* (default): same `Item` and equal `DataComponents.ENCHANTMENTS`.
  - *Similar* (`durabilityAutoReplaceSimilar`): any glider for a glider (`DataComponents.GLIDER`); any item
    equippable to the same armor slot (`Player.getEquipmentSlotForItem`, armor slots only); any item sharing
    one of the broken tool's tool tags (`#minecraft:swords`, `pickaxes`, `axes`, `shovels`, `hoes`); else
    same `Item`. The candidate with the **most remaining durability** wins.
  - The scan covers main inventory slots `0..35` plus the offhand (`Inventory.SLOT_OFFHAND`), excluding the
    inventory slot backing the target itself (selected hotbar slot for `MAINHAND`, `SLOT_OFFHAND` for
    `OFFHAND`, none for armor).
- **Apply (server):** `ServerInventoryHelper.applyDurabilityReplace(player, fromSlot, targetSlot, similar)`
  re-validates the request — `fromSlot` is a real inventory slot and not the target's own backing slot, the
  target item is damageable and actually `nextDamageWillBreak()`, the replacement `DurabilityReplace.matches`
  the target, and an armor target only accepts an item equippable there — then swaps the two slots via
  `Player.setItemSlot` + `Inventory.setItem`. Because it is a pure exchange it can never duplicate or lose
  items, and because the swap is a move the player could perform manually it needs no special permission.

---

## Config options

All paths are `ConfigPath.of("durability", "<key>")`. Defined in
`common/.../config/InventoryManagementConfig.java`. **Every option is `.clientOnly()`** — the client owns
all of them, consistent with the rest of the mod; nothing is server-side or synced.

| Option | Default | Notes |
|---|---|---|
| `durabilityAlertEnabled` | `true` | Master toggle for the alert. |
| `durabilityAlertThresholds` | `[10, 5]` | Percent thresholds (1-99). File-edited, `.noGuiControl()`. |
| `durabilityAlertAtOne` | `true` | Also alert at exactly 1 point remaining. |
| `durabilityAlertSound` | `true` | Play the anvil ping. |
| `durabilityAutoReplace` | `false` | Enable client-driven auto-replace. |
| `durabilityAutoReplaceSimilar` | `false` | Relax matching to "similar". |

---

## Client-driven request flow (no sync, no server config)

Trove config is a per-JVM singleton with **no config sync**, and we deliberately avoid syncing any
preference. Instead, auto-replace works exactly like the mod's other server actions (sort/stack/transfer):
the client makes the decision locally and sends a self-contained request.

- `network/Networking.java` — `DURABILITY_REPLACE_C2S` identifier + `DurabilityReplaceC2S(int fromSlot,
  EquipmentSlot targetSlot, boolean similar)` payload (`ByteBufCodecs.VAR_INT` + `EquipmentSlot.STREAM_CODEC`
  + `ByteBufCodecs.BOOL`). Registered in `register()`; the handler calls
  `ServerInventoryHelper.applyDurabilityReplace(...)`.
- `client/network/ClientNetworking.java` — `sendDurabilityReplace(fromSlot, targetSlot, similar)`.
- `common/.../durability/DurabilityReplace.java` — the shared matching/finding logic, callable from both
  sides (it only needs a `Player`; the client passes its `LocalPlayer`).

There is **no** per-player preference cache, no `serverOnly()` admin option, and no `hurtAndBreak` mixin.
A bad or stale request is simply rejected by the server-side re-validation.

---

## Scope & limitations (v1)

- **Auto-replace fires at the last durability point** (`nextDamageWillBreak()`, i.e. one use from
  breaking). For an action that would consume more than one durability from exactly two points remaining,
  the pre-emptive swap can be missed and the item breaks normally; the alert and a fresh manual equip still
  apply.
- **The worn-out item is kept**, not destroyed: the swap exchanges the two slots, so the near-dead item
  ends up in the inventory slot the replacement came from.
- **Multiplayer requires the mod on the server** (it performs and validates the swap). The alert needs
  nothing server-side.
