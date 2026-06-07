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

@Mixin(Gui.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarNumberIndicatorMixin {
  @Inject(method = "extractItemHotbar", at = @At("TAIL"))
  private void invMgmt$renderHotbarSwapBadge(
      GuiGraphicsExtractor graphics,
      DeltaTracker deltaTracker,
      CallbackInfo ci
  ) {
    HotbarSwapHud.render(graphics, deltaTracker);
  }
}
