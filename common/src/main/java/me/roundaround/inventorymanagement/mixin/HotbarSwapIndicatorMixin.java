package me.roundaround.inventorymanagement.mixin;

import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hotbar swapping — in-screen indicator. When a row is swapped into the hotbar, draws a small marker
 * just left of the leftmost slot of that swapped row in any inventory/container screen so the player
 * can see which main-inventory row is currently displaced.
 *
 * <p>Injected at the head of {@code extractSlots}, where the {@code translate(leftPos, topPos)} pushed
 * by {@code extractContents} is still active, so coordinates are slot-local (slots are 18x18 with a
 * 16x16 item area at {@code slot.x/slot.y}) — the same setup the locked-slot marker uses.
 */
@Mixin(AbstractContainerScreen.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class HotbarSwapIndicatorMixin {
  // Swapped-row marker colors (ARGB).
  private static final int INV_MGMT$SWAP_MARKER = 0xFFFFD54A;
  private static final int INV_MGMT$SWAP_MARKER_OUTLINE = 0x66000000;

  @Shadow
  protected abstract AbstractContainerMenu getMenu();

  @Inject(method = "extractSlots(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", at = @At("HEAD"))
  private void invMgmt$renderHotbarSwapMarker(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
    int row = HotbarSwapClient.getSwappedRow();
    if (row == 0) {
      return;
    }

    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    if (!config.isInitialized() || !config.modEnabled.getValue()) {
      return;
    }

    int leftmost = 9 + (row - 1) * 9;
    for (Slot slot : this.getMenu().slots) {
      if (!slot.isActive() || !(slot.container instanceof Inventory) || slot.getContainerSlot() != leftmost) {
        continue;
      }

      // Small marker in the gutter just left of the leftmost slot of the swapped row.
      graphics.outline(slot.x - 5, slot.y - 1, 4, 18, INV_MGMT$SWAP_MARKER_OUTLINE);
      graphics.fill(slot.x - 4, slot.y, slot.x - 2, slot.y + 16, INV_MGMT$SWAP_MARKER);
      break;
    }
  }
}
