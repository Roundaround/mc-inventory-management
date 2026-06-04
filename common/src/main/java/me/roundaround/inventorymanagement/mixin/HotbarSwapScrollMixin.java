package me.roundaround.inventorymanagement.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hotbar swapping — scroll path. While the hotbar-swap modifier is held in-world with no screen open,
 * scrolling cycles the swapped row instead of changing the selected hotbar slot.
 *
 * <p>Wraps the single {@code ScrollWheelHandler.getNextScrollWheelSelection(double, int, int)} call in
 * {@link MouseHandler#onScroll(long, double, double)} (the in-world branch). When the modifier is
 * active the scroll is consumed for row cycling and the current selection is returned unchanged so the
 * selected slot does not move; otherwise the original vanilla behaviour runs.
 */
@Mixin(MouseHandler.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarSwapScrollMixin {
  @WrapOperation(
      method = "onScroll",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"))
  private int invMgmt$rowScroll(double wheel, int current, int limit, Operation<Integer> original) {
    // The no-screen requirement is structurally guaranteed today: this INVOKE lives inside onScroll's
    // screen == null && player != null branch. The explicit check makes the in-world-only invariant
    // local to this mixin so a future vanilla refactor of onScroll's flow can't silently let a swap
    // fire with a screen open (isModifierActive() deliberately omits the screen check).
    if (Minecraft.getInstance().screen == null && HotbarSwapClient.isModifierActive()) {
      HotbarSwapClient.cycle(wheel);
      return current;
    }
    return original.call(wheel, current, limit);
  }
}
