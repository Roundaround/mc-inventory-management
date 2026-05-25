package me.roundaround.inventorymanagement.client.gui.screen;

import me.roundaround.inventorymanagement.mixin.HandledScreenAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public interface ScreenPositioner {
  int getX();

  int getY();

  int getBackgroundWidth();

  class HandledScreenWrapper implements ScreenPositioner {
    private final HandledScreenAccessor reference;

    public HandledScreenWrapper(AbstractContainerScreen<?> reference) {
      this.reference = (HandledScreenAccessor) reference;
    }

    @Override
    public int getX() {
      return this.reference.getX();
    }

    @Override
    public int getY() {
      return this.reference.getY();
    }

    @Override
    public int getBackgroundWidth() {
      return this.reference.getBackgroundWidth();
    }
  }
}
