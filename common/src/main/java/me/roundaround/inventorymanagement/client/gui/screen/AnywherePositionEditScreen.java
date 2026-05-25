package me.roundaround.inventorymanagement.client.gui.screen;

import me.roundaround.trove.client.gui.screen.PositionEditScreen;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.observable.Subscription;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AnywherePositionEditScreen extends PositionEditScreen {
  protected final Screen anywhereParent;

  public AnywherePositionEditScreen(Component title, Screen parent, PositionConfigOption configOption) {
    super(title, null, configOption);
    this.anywhereParent = parent;
    this.anywhereParent.setFocused(null);
  }

  @Override
  public void onClose() {
    this.subscriptions.forEach(Subscription::close);
    this.subscriptions.clear();
    this.minecraft.setScreen(this.anywhereParent);
  }
}
