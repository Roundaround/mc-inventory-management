package me.roundaround.inventorymanagement.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarNumberKeySwapMixin {
  @WrapOperation(
      method = "handleKeybinds", at = @At(
      value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"
  )
  )
  private void insertSelectedRowIndicator(Inventory inv, int selected, Operation<Void> original) {
    if (Minecraft.getInstance().gui.screen() == null && HotbarSwapClient.isModifierActive() &&
        HotbarSwapClient.numberKeysEnabled()) {
      if (selected >= 0 && selected <= 2) {
        HotbarSwapClient.pressRow(selected + 1);
      }
      return;
    }
    original.call(inv, selected);
  }
}
