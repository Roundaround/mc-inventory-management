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
 * DUR-01: a strict auto-replace of an about-to-break main-hand tool is a pure two-slot swap. Seeds
 * an about-to-break diamond pickaxe in the main hand and a fresh, identical diamond pickaxe in
 * inventory slot 14, requests {@code sendDurabilityReplace(14, MAINHAND, false)}, and asserts the
 * fresh pickaxe is now equipped (damage 0) while the worn one (damage {@code maxDamage - 1}) sits in
 * slot 14 — no duplication, no loss.
 */
@ClientGameTest
public class StrictReplaceMainhandTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      int maxDamage = new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage();

      world.setMainHandItem(ItemStacks.aboutToBreak(Items.DIAMOND_PICKAXE));
      world.setInventoryItem(14, new ItemStack(Items.DIAMOND_PICKAXE));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.MAINHAND, false));

      ItemStack mainhand = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.MAINHAND).copy());
      ItemStack slot14 = world.getInventoryItem(14);

      // The fresh pickaxe (damage 0) is now equipped; the worn one moved into slot 14.
      assertItem(mainhand, Items.DIAMOND_PICKAXE, 1);
      assertDamage(mainhand, 0);
      assertItem(slot14, Items.DIAMOND_PICKAXE, 1);
      assertDamage(slot14, maxDamage - 1);
    }
  }

  private static void assertDamage(ItemStack stack, int expected) {
    if (stack.getDamageValue() != expected) {
      throw new me.roundaround.trove.gametest.GameTestAssertionException(
          "expected damage " + expected + " but got " + stack.getDamageValue() + " on " + stack);
    }
  }
}
