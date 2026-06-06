package me.roundaround.inventorymanagement.client.gui.screen;

import me.roundaround.inventorymanagement.client.InventoryButtonsManager;
import me.roundaround.inventorymanagement.client.gui.AutoStackButton;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.inventorymanagement.client.gui.TransferAllButton;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.trove.client.gui.screen.ConfigScreen;
import me.roundaround.trove.client.gui.screen.PositionEditScreen;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.widget.config.SubScreenControl;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.LinkedList;

public class DefaultPositionEditScreen extends PositionEditScreen implements ScreenPositioner {
  private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace(
      "textures/gui/container/generic_54.png");
  private static final int BACKGROUND_WIDTH = 176;
  private static final int BACKGROUND_HEIGHT = 114 + 3 * 18;

  private final LinkedList<InventoryManagementButton> containerButtons = new LinkedList<>();
  private final LinkedList<InventoryManagementButton> playerButtons = new LinkedList<>();

  private DefaultPositionEditScreen(ConfigScreen parent, PositionConfigOption configOption) {
    super(Component.translatable("inventorymanagement.default_position_edit.title"), parent, configOption);
  }

  public static SubScreenControl.SubScreenFactory<Position, PositionConfigOption> getSubScreenFactory() {
    return DefaultPositionEditScreen::new;
  }

  @Override
  public void init() {
    super.init();

    this.containerButtons.clear();
    this.playerButtons.clear();

    InventoryManagementConfig config = InventoryManagementConfig.getInstance();

    Position offset = this.getOption().getPendingValue();

    Container containerInventory = new SimpleContainer(27);
    Slot containerSlot = new Slot(containerInventory, 8, BACKGROUND_WIDTH - 16 - 4, 6 + this.font.lineHeight + 3);
    int index = 0;

    if (config.showSort.getValue()) {
      this.containerButtons.add(new SortInventoryButton(
          this,
          containerInventory,
          containerSlot,
          InventoryButtonsManager.INSTANCE.getButtonPosition(index++, offset),
          true
      ));
    }
    if (config.showStack.getValue()) {
      this.containerButtons.add(new AutoStackButton(
          this,
          containerInventory,
          containerSlot,
          InventoryButtonsManager.INSTANCE.getButtonPosition(index++, offset),
          true
      ));
    }
    if (config.showTransfer.getValue()) {
      this.containerButtons.add(new TransferAllButton(
          this,
          containerInventory,
          containerSlot,
          InventoryButtonsManager.INSTANCE.getButtonPosition(index++, offset),
          true
      ));
    }

    Container playerInventory = new SimpleContainer(27);
    Slot playerSlot = new Slot(
        playerInventory,
        8,
        BACKGROUND_WIDTH - 16 - 4,
        BACKGROUND_HEIGHT - 94 + this.font.lineHeight + 2
    );
    index = 0;

    if (config.showSort.getValue()) {
      this.playerButtons.add(new SortInventoryButton(
          this,
          playerInventory,
          playerSlot,
          InventoryButtonsManager.INSTANCE.getButtonPosition(index++, offset),
          true
      ));
    }
    if (config.showStack.getValue()) {
      this.playerButtons.add(new AutoStackButton(
          this,
          playerInventory,
          playerSlot,
          InventoryButtonsManager.INSTANCE.getButtonPosition(index++, offset),
          true
      ));
    }
    if (config.showTransfer.getValue()) {
      this.playerButtons.add(new TransferAllButton(
          this,
          playerInventory,
          playerSlot,
          InventoryButtonsManager.INSTANCE.getButtonPosition(index++, offset),
          true
      ));
    }
  }

  @Override
  protected boolean supportsDragging() {
    return true;
  }

  @Override
  protected boolean isInDraggableRegion(double mouseX, double mouseY) {
    return this.isOverAnyButton(this.containerButtons, mouseX, mouseY) ||
        this.isOverAnyButton(this.playerButtons, mouseX, mouseY);
  }

  private boolean isOverAnyButton(LinkedList<InventoryManagementButton> buttons, double mouseX, double mouseY) {
    return buttons.stream().anyMatch((button) -> button.containsPoint(mouseX, mouseY));
  }

  @Override
  protected void setValue(Position value) {
    super.setValue(value);
    this.refreshButtonPositions();
  }

  @Override
  protected void resetToDefault() {
    super.resetToDefault();
    this.refreshButtonPositions();
  }

  @Override
  public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

    this.containerButtons.forEach((button) -> button.extractRenderState(graphics, mouseX, mouseY, partialTicks));
    this.playerButtons.forEach((button) -> button.extractRenderState(graphics, mouseX, mouseY, partialTicks));

    graphics.text(
        this.font,
        Component.literal(this.getValue().toString()),
        GuiUtil.PADDING,
        GuiUtil.PADDING,
        GuiUtil.LABEL_COLOR
    );
  }

  @Override
  public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    if (this.minecraft.level == null) {
      this.extractPanorama(graphics, delta);
    }

    this.extractBlurredBackground(graphics);
    this.extractMenuBackground(graphics);

    int x = (this.width - BACKGROUND_WIDTH) / 2;
    int y = (this.height - BACKGROUND_HEIGHT) / 2;
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        BACKGROUND_TEXTURE,
        x,
        y,
        0,
        0,
        BACKGROUND_WIDTH,
        3 * 18 + 17,
        256,
        256
    );
    graphics.blit(
        RenderPipelines.GUI_TEXTURED,
        BACKGROUND_TEXTURE,
        x,
        y + 3 * 18 + 17,
        0,
        126,
        BACKGROUND_WIDTH,
        96,
        256,
        256
    );

    Matrix3x2fStack matrices = graphics.pose();
    matrices.pushMatrix();
    matrices.translate(x, y);
    graphics.text(this.font, Component.translatable("container.chest"), 8, 6, 0x404040, false);
    graphics.text(this.font, Component.translatable("container.inventory"), 8, BACKGROUND_HEIGHT - 94, 0x404040, false);
    matrices.popMatrix();
  }

  @Override
  public int getX() {
    return (this.width - BACKGROUND_WIDTH) / 2;
  }

  @Override
  public int getY() {
    return (this.height - BACKGROUND_HEIGHT) / 2;
  }

  @Override
  public int getBackgroundWidth() {
    return BACKGROUND_WIDTH;
  }

  private void refreshButtonPositions() {
    Position offset = this.getValue();
    for (int i = 0; i < this.containerButtons.size(); i++) {
      this.containerButtons.get(i).setOffset(InventoryButtonsManager.INSTANCE.getButtonPosition(i, offset));
    }
    for (int i = 0; i < this.playerButtons.size(); i++) {
      this.playerButtons.get(i).setOffset(InventoryButtonsManager.INSTANCE.getButtonPosition(i, offset));
    }
  }
}
