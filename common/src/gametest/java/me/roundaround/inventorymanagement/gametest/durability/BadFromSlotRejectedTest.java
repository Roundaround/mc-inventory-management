package me.roundaround.inventorymanagement.gametest.durability;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.gametest.InvGameTests;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ItemStacks;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static me.roundaround.trove.gametest.GameTestAssertions.assertItem;

/**
 * DUR-06: the server rejects a structurally bad {@code fromSlot} for an otherwise-valid about-to-break
 * main hand, proving the slot guards run before any swap. Two no-op cases on the same seeded about-to-break
 * diamond pickaxe (with a matching replacement in slot 14 so the only thing wrong is the slot index):
 * <ul>
 *   <li>(a) {@code fromSlot = 99} — out of the legal {@code 0..35}/offhand range, so the guard
 *       {@code (fromSlot < 0 || fromSlot >= 36) && fromSlot != SLOT_OFFHAND} returns early.</li>
 *   <li>(b) {@code fromSlot = } the selected hotbar slot backing the main hand — the guard
 *       {@code fromSlot == backingInventorySlot(MAINHAND)} returns early, since you cannot use the target's
 *       own backing slot as the source.</li>
 * </ul>
 * Each case asserts the worn pickaxe stays equipped and the replacement stays in slot 14.
 */
@ClientGameTest
public class BadFromSlotRejectedTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      int maxDamage = new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage();

      world.setMainHandItem(ItemStacks.aboutToBreak(Items.DIAMOND_PICKAXE));
      // A genuinely valid replacement sits in slot 14: were the slot index legal, this WOULD swap, so a
      // no-op isolates the bad-slot guard as the cause.
      world.setInventoryItem(14, new ItemStack(Items.DIAMOND_PICKAXE));
      context.waitTicks(2);

      // ---- Case (a): out-of-range fromSlot. ----
      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(99, EquipmentSlot.MAINHAND, false));
      assertNoSwap(world, maxDamage, "case (a) fromSlot=99");

      // ---- Case (b): fromSlot == the selected hotbar slot that backs the main hand. ----
      int selectedSlot = world.computeOnServerPlayer(p -> p.getInventory().getSelectedSlot());
      InvGameTests.act(context,
          mc -> ClientNetworking.sendDurabilityReplace(selectedSlot, EquipmentSlot.MAINHAND, false));
      assertNoSwap(world, maxDamage, "case (b) fromSlot==selected hotbar slot " + selectedSlot);
    }
  }

  /** Assert the worn about-to-break pickaxe is still equipped and the slot-14 replacement is untouched. */
  private static void assertNoSwap(ClientWorld world, int maxDamage, String where) {
    ItemStack mainhand = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.MAINHAND).copy());
    ItemStack slot14 = world.getInventoryItem(14);

    assertItem(mainhand, Items.DIAMOND_PICKAXE, 1);
    if (mainhand.getDamageValue() != maxDamage - 1) {
      throw new me.roundaround.trove.gametest.GameTestAssertionException(
          where + ": expected the worn pickaxe (damage " + (maxDamage - 1)
              + ") to stay equipped, got damage " + mainhand.getDamageValue());
    }
    assertItem(slot14, Items.DIAMOND_PICKAXE, 1);
    if (slot14.getDamageValue() != 0) {
      throw new me.roundaround.trove.gametest.GameTestAssertionException(
          where + ": expected the fresh replacement (damage 0) to stay in slot 14, got damage "
              + slot14.getDamageValue());
    }
  }
}
