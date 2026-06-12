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
 * DUR-05: auto-replace is a no-op when the target is damageable but NOT about to break. The server gate
 * {@code !current.nextDamageWillBreak()} returns early, so even a perfectly valid replacement is ignored.
 * Seeds a half-worn diamond pickaxe (damage {@code maxDamage / 2}) in the main hand and a fresh, strictly
 * matching diamond pickaxe in slot 14, requests {@code sendDurabilityReplace(14, MAINHAND, false)}, and
 * asserts NO swap — the half-worn pickaxe stays equipped and the fresh one stays in slot 14.
 */
@ClientGameTest
public class NotAboutToBreakNoOpTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      int maxDamage = new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage();
      int halfDamage = maxDamage / 2;

      world.setMainHandItem(ItemStacks.withDamage(Items.DIAMOND_PICKAXE, halfDamage));
      world.setInventoryItem(14, new ItemStack(Items.DIAMOND_PICKAXE));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.MAINHAND, false));

      ItemStack mainhand = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.MAINHAND).copy());
      ItemStack slot14 = world.getInventoryItem(14);

      // No swap: the half-worn pickaxe is still equipped (damage unchanged) and slot 14 is untouched.
      assertItem(mainhand, Items.DIAMOND_PICKAXE, 1);
      if (mainhand.getDamageValue() != halfDamage) {
        throw new me.roundaround.trove.gametest.GameTestAssertionException(
            "expected the half-worn pickaxe (damage " + halfDamage + ") to stay equipped, got damage "
                + mainhand.getDamageValue());
      }
      assertItem(slot14, Items.DIAMOND_PICKAXE, 1);
      if (slot14.getDamageValue() != 0) {
        throw new me.roundaround.trove.gametest.GameTestAssertionException(
            "expected the fresh replacement (damage 0) to stay in slot 14, got damage " + slot14.getDamageValue());
      }
    }
  }
}
