package me.roundaround.inventorymanagement.client.gui.screen;

import me.roundaround.inventorymanagement.client.InventoryButtonsManager;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.ScreenWidgets;
import me.roundaround.trove.client.gui.widget.IconButtonWidget;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;

public class PerScreenPositionEditScreen extends AnywherePositionEditScreen {
  private final LinkedList<InventoryManagementButton> buttons = new LinkedList<>();

  private final boolean isPlayerInventory;

  public PerScreenPositionEditScreen(Screen parent, boolean isPlayerInventory) {
    super(computeTitle(parent, isPlayerInventory), parent, generateDummyConfigOption(parent, isPlayerInventory));
    this.isPlayerInventory = isPlayerInventory;
  }

  // Static because it feeds super(...) before fields are assigned; derives the screen name from the
  // ctor arg `parent` (this.anywhereParent is not yet set at this point).
  private static Component computeTitle(Screen parent, boolean isPlayerInventory) {
    Component sideName = Component.translatable(isPlayerInventory ?
        "inventorymanagement.position_edit.side.player" :
        "inventorymanagement.position_edit.side.container");
    return Component.translatable("inventorymanagement.position_edit.title.scoped", parent.getTitle(), sideName);
  }

  private static PositionConfigOption generateDummyConfigOption(Screen parent, boolean isPlayerInventory) {
    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    Position defaultValue = config.defaultPosition.getValue();
    Position currentValue = config.screenPositions.get(parent, isPlayerInventory).orElse(defaultValue);

    PositionConfigOption option = PositionConfigOption.builder(config.screenPositions.getPath())
        .setDefaultValue(defaultValue)
        .build();
    option.setModId(Constants.MOD_ID);
    option.setValue(currentValue);

    return option;
  }

  @Override
  protected void initElements() {
    if (this.hasOtherSideButtons()) {
      IconButtonWidget switchButton = IconButtonWidget.builder(BuiltinIcon.NEXT_18, this.modId)
          .dimensions(IconButtonWidget.SIZE_L)
          .messageAndTooltip(Component.translatable(this.isPlayerInventory ?
              "inventorymanagement.position_edit.switch_side.to_container" :
              "inventorymanagement.position_edit.switch_side.to_player"))
          .onPress((button) -> this.minecraft.setScreen(new PerScreenPositionEditScreen(
              this.anywhereParent,
              !this.isPlayerInventory
          )))
          .build();
      this.nonPositioningRoot.add(
          switchButton,
          (parent, self) -> self.setPosition(
              parent.getX() + parent.getWidth() - GuiUtil.PADDING - self.getWidth(),
              parent.getY() + GuiUtil.PADDING
          )
      );
    }

    super.initElements();
  }

  private boolean hasOtherSideButtons() {
    return !(this.isPlayerInventory ?
        InventoryButtonsManager.INSTANCE.getContainerButtons() :
        InventoryButtonsManager.INSTANCE.getPlayerButtons()).isEmpty();
  }

  @Override
  protected void init() {
    super.init();

    this.subscriptions.add(this.getOption().pendingValue.subscribe((value) -> {
      InventoryManagementConfig.getInstance().screenPositions.set(this.anywhereParent, this.isPlayerInventory, value);
      this.refreshButtonPositions(value);
    }));

    this.buttons.addAll(this.isPlayerInventory ?
        InventoryButtonsManager.INSTANCE.getPlayerButtons() :
        InventoryButtonsManager.INSTANCE.getContainerButtons());

    ScreenWidgets.getWidgets(this.anywhereParent).removeIf((widget) -> widget instanceof InventoryManagementButton);

    this.refreshButtonPositions(this.getValue());
  }

  @Override
  protected boolean supportsDragging() {
    return true;
  }

  @Override
  protected boolean isInDraggableRegion(double mouseX, double mouseY) {
    return this.buttons.stream().anyMatch((button) -> button.containsPoint(mouseX, mouseY));
  }

  @Override
  public void onClose() {
    super.onClose();
    InventoryManagementConfig.getInstance().writeToStore();
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    context.nextStratum();
    this.anywhereParent.extractBackground(context, mouseX, mouseY, delta);
    context.nextStratum();
    this.anywhereParent.extractRenderState(context, mouseX, mouseY, delta);
    context.nextStratum();

    super.extractRenderState(context, mouseX, mouseY, delta);

    this.buttons.forEach((button) -> {
      button.clearSelected();
      button.extractRenderState(context, mouseX, mouseY, delta);
    });
    context.text(
        this.font,
        Component.literal(this.getValue().toString()),
        GuiUtil.PADDING,
        GuiUtil.PADDING,
        GuiUtil.LABEL_COLOR
    );
  }

  private void refreshButtonPositions(Position value) {
    for (int i = 0; i < this.buttons.size(); i++) {
      this.buttons.get(i).setOffset(InventoryButtonsManager.INSTANCE.getButtonPosition(i, value));
    }
  }
}
