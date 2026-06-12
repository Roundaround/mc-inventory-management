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
 * DUR-03: "similar" matching accepts a same-category tool while strict does not. An iron pickaxe shares
 * the {@code minecraft:pickaxes} tool tag with a diamond pickaxe, so it is a valid SIMILAR replacement but
 * NOT a strict one (different item). Two phases on the same about-to-break main-hand diamond pickaxe, with
 * state reset between:
 * <ul>
 *   <li>Phase A — {@code similar = false}: the iron pickaxe is rejected, the diamond pickaxe stays equipped.</li>
 *   <li>Phase B — {@code similar = true}: the iron pickaxe swaps in and the worn diamond pickaxe lands in slot 14.</li>
 * </ul>
 */
@ClientGameTest
public class SimilarAcceptsSameCategoryTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      int diamondMaxDamage = new ItemStack(Items.DIAMOND_PICKAXE).getMaxDamage();

      // ---- Phase A: strict rejects the cross-material candidate. ----
      seed(world, context);
      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.MAINHAND, false));

      ItemStack mainhandA = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.MAINHAND).copy());
      ItemStack slot14A = world.getInventoryItem(14);
      assertItem(mainhandA, Items.DIAMOND_PICKAXE, 1);
      if (mainhandA.getDamageValue() != diamondMaxDamage - 1) {
        throw new me.roundaround.trove.gametest.GameTestAssertionException(
            "strict phase: expected the worn diamond pickaxe to stay equipped, got damage "
                + mainhandA.getDamageValue());
      }
      assertItem(slot14A, Items.IRON_PICKAXE, 1);

      // ---- Phase B: reset, then similar accepts the same candidate. ----
      seed(world, context);
      InvGameTests.act(context, mc -> ClientNetworking.sendDurabilityReplace(14, EquipmentSlot.MAINHAND, true));

      ItemStack mainhandB = world.computeOnServerPlayer(p -> p.getItemBySlot(EquipmentSlot.MAINHAND).copy());
      ItemStack slot14B = world.getInventoryItem(14);
      assertItem(mainhandB, Items.IRON_PICKAXE, 1);
      assertItem(slot14B, Items.DIAMOND_PICKAXE, 1);
      if (slot14B.getDamageValue() != diamondMaxDamage - 1) {
        throw new me.roundaround.trove.gametest.GameTestAssertionException(
            "similar phase: expected the worn diamond pickaxe (damage " + (diamondMaxDamage - 1)
                + ") to land in slot 14, got damage " + slot14B.getDamageValue());
      }
    }
  }

  /** Reset to the starting state: about-to-break diamond pickaxe in hand, fresh iron pickaxe in slot 14. */
  private static void seed(ClientWorld world, ClientTestContext context) {
    world.setMainHandItem(ItemStacks.aboutToBreak(Items.DIAMOND_PICKAXE));
    world.setInventoryItem(14, new ItemStack(Items.IRON_PICKAXE));
    context.waitTicks(2);
  }
}
