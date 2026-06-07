package me.roundaround.inventorymanagement.mixin;

import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.gui.hud.HotbarSwapHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hotbar swapping — in-game HUD indicator. Draws the swapped-row badge just left of the vanilla
 * hotbar by appending to {@code Gui#extractItemHotbar} (TAIL), so the draw runs inside the hotbar
 * layer's own clean stratum.
 *
 * <p>This is why it doesn't need a real HUD layer: a TAIL on the outer {@code Gui#extractRenderState}
 * would be silently culled by MC 26.1's {@code GuiRenderState} (its bounds resolve to null because it
 * inherits the scissor/stratum state the vanilla HUD layers leave behind), but the per-layer
 * {@code extractItemHotbar} method runs with clean state. Same approach as Sprint Indicator's
 * {@code GuiMixin}.
 */
@Mixin(Gui.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarSwapHudMixin {
  @Inject(method = "extractItemHotbar", at = @At("TAIL"))
  private void invMgmt$renderHotbarSwapBadge(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
    HotbarSwapHud.render(context, tickCounter);
  }
}
