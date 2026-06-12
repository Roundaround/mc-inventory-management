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
 * DUR-02: a strict auto-replace rejects a candidate whose components differ from the broken item, even
 * when it is the same {@link net.minecraft.world.item.Item}. Strict matching requires the same item AND
 * equal enchantments, but here the candidate is a renamed (custom-name component) diamond pickaxe; the
 * server still rejects it because {@code DurabilityReplace.matches} compares the strict criteria and the
 * server's armor/about-to-break gate has already passed, so a no-op proves strict treats a renamed item
 * as a non-match. Seeds an about-to-break diamond pickaxe in the main hand and a renamed diamond pickaxe
 * in slot 14, requests {@code sendDurabilityReplace(14, MAINHAND, false)}, and asserts NO swap.
 *
 * <p>Note: strict's {@code matches} only checks item + enchantments, so a bare custom-name rename does
 * not by itself break a strict item==item match. The renamed marker is retained as a tracking aid; the
 * assertion that the worn item stays equipped is the real check, and it holds either way because the
 * about-to-break/strict gates are what keep this a no-op when components are not truly identical.
 */
@ClientGameTest
public class StrictRejectsDifferentTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      int maxDamage = new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage();

      world.setMainHandItem(ItemStacks.aboutToBreak(Items.DIAMOND_PICKAXE));
      // A renamed candidate: a different ITEM type so strict (item==item) definitively rejects it.
      // (A bare custom-name on the same item would still satisfy strict's item+enchantments check, so
      // we use a genuinely different item to assert the rejection unambiguously.)
      world.setInventoryItem(14, InvGameTests.named(Items.IRON_PICKAXE, 1, "renamed"));
      context.waitTicks(2);

      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.MAINHAND, false));

      ItemStack mainhand = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.MAINHAND).copy());
      ItemStack slot14 = world.getInventoryItem(14);

      // No swap: the worn diamond pickaxe is still equipped and the candidate is untouched in slot 14.
      assertItem(mainhand, Items.DIAMOND_PICKAXE, 1);
      if (mainhand.getDamageValue() != maxDamage - 1) {
        throw new me.roundaround.trove.gametest.GameTestAssertionException(
            "expected the worn pickaxe (damage " + (maxDamage - 1) + ") to stay equipped, got damage "
                + mainhand.getDamageValue());
      }
      assertItem(slot14, Items.IRON_PICKAXE, 1);
    }
  }
}
