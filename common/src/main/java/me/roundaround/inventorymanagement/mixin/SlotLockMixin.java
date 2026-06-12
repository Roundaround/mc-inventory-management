package me.roundaround.inventorymanagement.mixin;

import me.roundaround.allay.api.MixinEnv;
import me.roundaround.inventorymanagement.client.InventoryManagementKeyMappings;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.LockedSlotDisplay;
import me.roundaround.inventorymanagement.inventory.IgnoredSlots;
import me.roundaround.trove.client.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class SlotLockMixin {
  @Unique
  private static final int INV_MGMT$LOCK_BG_TINT = 0x50000000;

  @Unique
  private static final int INV_MGMT$LOCK_BORDER = 0x22FFFFFF;

  @Unique
  private static final Component INV_MGMT$LOCK_TOOLTIP = Component.translatable(
      "inventorymanagement.tooltip.slot_locked");

  @Shadow
  @Nullable
  protected Slot hoveredSlot;

  @Shadow
  public abstract AbstractContainerMenu getMenu();

  @Shadow
  protected abstract List<Component> getTooltipFromContainerItem(ItemStack itemStack);

  @Inject(
      method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("HEAD"), cancellable = true
  )
  private void invMgmt$toggleLockOnCtrlClick(
      MouseButtonEvent event,
      boolean doubleClick,
      CallbackInfoReturnable<Boolean> cir
  ) {
    if (!InventoryManagementConfig.getInstance().isSlotLockingEnabled()) {
      return;
    }
    if (!Minecraft.getInstance().hasControlDown()) {
      return;
    }
    if (event.button() != 0 && event.button() != 1) {
      return;
    }

    Slot slot = this.hoveredSlot;
    if (slot == null || !(slot.container instanceof Inventory)) {
      return;
    }

    int index = slot.getContainerSlot();
    if (!IgnoredSlots.isLockable(index)) {
      return;
    }

    InventoryManagementConfig.getInstance().toggleLockedPlayerSlot(index);
    cir.setReturnValue(true);
  }

  /**
   * Before slot contents are drawn, mark each locked player slot with a darkened background and a
   * border so the marker renders <em>underneath</em> the item. Injected at the head of
   * {@code extractSlots}, where the {@code translate(leftPos, topPos)} pushed by {@code extractContents}
   * is still active, so coordinates are slot-local. Slots are 18x18: a 16x16 item area at
   * {@code slot.x/slot.y} with a 1px surrounding gutter.
   */
  @Inject(method = "extractSlots(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V", at = @At("HEAD"))
  private void invMgmt$renderLockMarkers(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
    List<Integer> locked = InventoryManagementConfig.getInstance().getLockedPlayerSlots();
    if (locked.isEmpty() || !invMgmt$shouldRenderLockMarkers()) {
      return;
    }

    for (Slot slot : this.getMenu().slots) {
      if (!slot.isActive() || !(slot.container instanceof Inventory)) {
        continue;
      }
      if (!locked.contains(slot.getContainerSlot())) {
        continue;
      }

      // Darken the 16x16 item background; shows through transparent item pixels and on empty slots.
      graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, INV_MGMT$LOCK_BG_TINT);
      // Frame the full 18x18 cell; the 1px gutter ring stays visible even under a full-bleed item.
      graphics.outline(slot.x - 1, slot.y - 1, 18, 18, INV_MGMT$LOCK_BORDER);
    }
  }

  /**
   * Whether the locked-slot marker (background + border) should be drawn this frame, per the
   * {@code lockedSlotDisplay} config option: always in {@link LockedSlotDisplay#SHOWN}, never in
   * {@link LockedSlotDisplay#HIDDEN}, and only while the "Peek locked slots" keybind is physically held
   * in {@link LockedSlotDisplay#HOTKEY} mode. The hover tooltip is independent of this and keeps locked
   * slots discoverable even when the marker is hidden.
   */
  private static boolean invMgmt$shouldRenderLockMarkers() {
    if (!InventoryManagementConfig.getInstance().isSlotLockingEnabled()) {
      return false;
    }
    return switch (InventoryManagementConfig.getInstance().getLockedSlotDisplay()) {
      case SHOWN -> true;
      case HIDDEN -> false;
      case HOTKEY -> KeyBindings.isHeld(InventoryManagementKeyMappings.peekLockedSlots);
    };
  }

  /**
   * When a locked player slot is hovered, queue a tooltip that appends the lock line below the item's
   * normal lines (or shows just the lock line for an empty slot), then cancel the vanilla tooltip so it
   * does not also fire. Queued before vanilla runs ({@code extractTooltip} is called after
   * {@code extractContents} in {@code extractRenderState}), so it becomes the frame's deferred tooltip.
   */
  @Inject(
      method = "extractTooltip(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V",
      at = @At("HEAD"),
      cancellable = true
  )
  private void invMgmt$appendLockTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
    if (!InventoryManagementConfig.getInstance().isSlotLockingEnabled()) {
      return;
    }
    Slot slot = this.hoveredSlot;
    if (slot == null || !(slot.container instanceof Inventory) || !IgnoredSlots.isLockable(slot.getContainerSlot())) {
      return;
    }
    if (!InventoryManagementConfig.getInstance().getLockedPlayerSlots().contains(slot.getContainerSlot())) {
      return;
    }

    Font font = Minecraft.getInstance().font;
    List<Component> lines = new ArrayList<>();
    if (slot.hasItem() && this.getMenu().getCarried().isEmpty()) {
      lines.addAll(this.getTooltipFromContainerItem(slot.getItem()));
    }
    lines.add(INV_MGMT$LOCK_TOOLTIP);

    graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
    ci.cancel();
  }
}
