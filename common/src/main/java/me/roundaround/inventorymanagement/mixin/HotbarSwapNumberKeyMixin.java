package me.roundaround.inventorymanagement.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hotbar swapping — number-key path (config-gated, default off). While the hotbar-swap modifier is
 * held and the {@code hotbarSwapNumberKeys} option is on, number keys 1-3 select rows 1-3 instead of
 * changing the selected hotbar slot; pressing the number matching the active row toggles back to the
 * normal layout. Keys 4-9 are consumed (no slot change). When the option is off or the modifier is not
 * held, number keys behave normally.
 *
 * <p>Wraps the single {@code Inventory.setSelectedSlot(int)} call in the {@code i in 0..8} loop of
 * {@link Minecraft#handleKeybinds()}.
 */
@Mixin(Minecraft.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarSwapNumberKeyMixin {
  @WrapOperation(
      method = "handleKeybinds",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
  private void invMgmt$numberKeyRow(Inventory inv, int i, Operation<Void> original) {
    // In-world only: this INVOKE lives in handleKeybinds, which vanilla only calls when screen == null,
    // and KeyMapping.click queues clicks only with no screen open. The explicit check makes the
    // requirement local to this mixin (matching HotbarSwapScrollMixin) rather than relying on vanilla's
    // click-queuing internals.
    if (Minecraft.getInstance().screen == null && HotbarSwapClient.isModifierActive()
        && HotbarSwapClient.numberKeysEnabled()) {
      if (i >= 0 && i <= 2) {
        HotbarSwapClient.pressRow(i + 1);
      }
      return;
    }
    original.call(inv, i);
  }
}
