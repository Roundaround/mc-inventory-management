package me.roundaround.inventorymanagement.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarScrollSwapMixin {
  @WrapOperation(
      method = "onScroll", at = @At(
      value = "INVOKE", target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I"
  )
  )
  private int handleHotbarSwapScroll(double wheel, int currentSelected, int limit, Operation<Integer> original) {
    if (Minecraft.getInstance().screen == null && HotbarSwapClient.isModifierActive()) {
      HotbarSwapClient.cycle(wheel);
      return currentSelected;
    }
    return original.call(wheel, currentSelected, limit);
  }
}
