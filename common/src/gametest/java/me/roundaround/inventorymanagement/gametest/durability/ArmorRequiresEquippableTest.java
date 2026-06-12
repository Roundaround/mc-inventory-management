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
 * DUR-04: an armor-slot auto-replace only accepts a candidate that is itself equippable in that slot. The
 * server gate {@code targetSlot.isArmor() && getEquipmentSlotForItem(replacement) != targetSlot} rejects
 * anything that does not belong on the head. Two phases on the same about-to-break HEAD helmet, reset
 * between:
 * <ul>
 *   <li>Phase A — replacement is another iron helmet: it swaps in (worn helmet lands in slot 14).</li>
 *   <li>Phase B — replacement is a sword (equips to the main hand, not the head): rejected, no swap.</li>
 * </ul>
 *
 * <p>The HEAD equipment is seeded with {@code setItemSlot} on the server player (Trove exposes no
 * {@code setEquipment(EquipmentSlot, ItemStack)} overload, and the string/Item overloads cannot carry the
 * about-to-break damage component), then read back with {@code getItemBySlot}.
 */
@ClientGameTest
public class ArmorRequiresEquippableTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      int helmetMaxDamage = new ItemStack(Items.IRON_HELMET).getMaxDamage();

      // ---- Phase A: an equippable head item is accepted. ----
      seedHelmet(world, context);
      world.setInventoryItem(14, new ItemStack(Items.IRON_HELMET));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.HEAD, false));

      ItemStack headA = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.HEAD).copy());
      ItemStack slot14A = world.getInventoryItem(14);
      assertItem(headA, Items.IRON_HELMET, 1);
      assertDamage(headA, 0, "phase A head");
      assertItem(slot14A, Items.IRON_HELMET, 1);
      assertDamage(slot14A, helmetMaxDamage - 1, "phase A slot 14 (worn helmet)");

      // ---- Phase B: reset, then a non-head item is rejected. ----
      seedHelmet(world, context);
      world.setInventoryItem(14, new ItemStack(Items.DIAMOND_SWORD));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.HEAD, false));

      ItemStack headB = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.HEAD).copy());
      ItemStack slot14B = world.getInventoryItem(14);
      // No swap: the worn helmet stays on the head and the sword stays in slot 14.
      assertItem(headB, Items.IRON_HELMET, 1);
      assertDamage(headB, helmetMaxDamage - 1, "phase B head (worn helmet should remain)");
      assertItem(slot14B, Items.DIAMOND_SWORD, 1);
    }
  }

  private static void seedHelmet(ClientWorld world, ClientTestContext context) {
    world.runOnServerPlayer(p ->
        p.setItemSlot(EquipmentSlot.HEAD, ItemStacks.aboutToBreak(Items.IRON_HELMET).copy()));
    context.waitTicks(2);
  }

  private static void assertDamage(ItemStack stack, int expected, String where) {
    if (stack.getDamageValue() != expected) {
      throw new me.roundaround.trove.gametest.GameTestAssertionException(
          where + ": expected damage " + expected + " but got " + stack.getDamageValue() + " on " + stack);
    }
  }
}
