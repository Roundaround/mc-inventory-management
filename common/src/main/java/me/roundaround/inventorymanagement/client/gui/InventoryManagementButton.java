package me.roundaround.inventorymanagement.client.gui;

import me.roundaround.inventorymanagement.client.gui.screen.PerScreenPositionEditScreen;
import me.roundaround.inventorymanagement.client.gui.screen.ScreenPositioner;
import me.roundaround.inventorymanagement.mixin.AbstractContainerScreenAccessor;
import me.roundaround.inventorymanagement.mixin.ScreenAccessor;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public abstract class InventoryManagementButton extends Button {
  public static final int WIDTH = 14;
  public static final int HEIGHT = 14;

  private final ScreenPositioner positioner;
  private final Slot referenceSlot;

  private final WidgetSprites textures;

  private Position offset;

  protected InventoryManagementButton(
      AbstractContainerScreen<?> parent,
      Container inventory,
      Slot referenceSlot,
      Position offset,
      boolean isPlayerInventory,
      OnPress onPress,
      net.minecraft.network.chat.Component tooltip,
      WidgetSprites textures
  ) {
    super(
        ((AbstractContainerScreenAccessor) parent).getX() + ((AbstractContainerScreenAccessor) parent).getBackgroundWidth() + offset.x(),
        ((AbstractContainerScreenAccessor) parent).getY() + referenceSlot.y + offset.y(),
        WIDTH,
        HEIGHT,
        CommonComponents.EMPTY,
        (button) -> {
          if (!((ScreenAccessor) parent).getMinecraft().hasControlDown()) {
            onPress.onPress(button);
            return;
          }

          GuiUtil.setScreen(new PerScreenPositionEditScreen(parent, isPlayerInventory));
        },
        DEFAULT_NARRATION
    );

    this.positioner = new ScreenPositioner.HandledScreenWrapper(parent);
    this.referenceSlot = referenceSlot;
    this.textures = textures;
    this.offset = offset;

    this.setTooltip(Tooltip.create(tooltip));
  }

  protected InventoryManagementButton(
      ScreenPositioner parent,
      Container inventory,
      Slot referenceSlot,
      Position offset,
      boolean isPlayerInventory,
      OnPress onPress,
      net.minecraft.network.chat.Component tooltip,
      WidgetSprites textures
  ) {
    super(
        parent.getX() + parent.getBackgroundWidth() + offset.x(),
        parent.getY() + referenceSlot.y + offset.y(),
        WIDTH,
        HEIGHT,
        CommonComponents.EMPTY,
        (button) -> {
        },
        DEFAULT_NARRATION
    );

    this.positioner = parent;
    this.referenceSlot = referenceSlot;
    this.textures = textures;
    this.offset = offset;

    this.setTooltip(Tooltip.create(tooltip));
  }

  public void setOffset(Position position) {
    this.offset = position;
  }

  @Override
  public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    this.setX(this.positioner.getX() + this.positioner.getBackgroundWidth() + this.offset.x());
    this.setY(this.positioner.getY() + this.referenceSlot.y + this.offset.y());

    Identifier texture = this.textures.get(this.isActive(), this.isHoveredOrFocused());
    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.width, this.height);
  }

  public void clearSelected() {
    this.isHovered = false;
    this.setFocused(false);
  }

  /**
   * Bounds test against the button's last-rendered rectangle, independent of {@code active}/
   * {@code visible} (unlike {@link #isMouseOver}). Used by the position editors to start a
   * click-and-drag from anywhere on a button.
   */
  public boolean containsPoint(double mouseX, double mouseY) {
    return mouseX >= this.getX() && mouseX < this.getX() + this.getWidth() && mouseY >= this.getY() &&
        mouseY < this.getY() + this.getHeight();
  }
}
