package me.roundaround.inventorymanagement.client.gui;

import me.roundaround.inventorymanagement.client.gui.screen.ScreenPositioner;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class SortInventoryButton extends InventoryManagementButton {
  private static final WidgetSprites TEXTURES = new WidgetSprites(
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sort"),
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sort_highlighted")
  );

  public SortInventoryButton(
      AbstractContainerScreen<?> parent,
      Container inventory,
      Slot referenceSlot,
      Position offset,
      boolean isPlayerInventory
  ) {
    super(
        parent,
        inventory,
        referenceSlot,
        offset,
        isPlayerInventory,
        (button) -> ClientNetworking.sendSort(isPlayerInventory),
        getTooltip(isPlayerInventory),
        TEXTURES
    );
  }

  public SortInventoryButton(
      ScreenPositioner parent,
      Container inventory,
      Slot referenceSlot,
      Position offset,
      boolean isPlayerInventory
  ) {
    super(
        parent,
        inventory,
        referenceSlot,
        offset,
        isPlayerInventory,
        (button) -> ClientNetworking.sendSort(isPlayerInventory),
        getTooltip(isPlayerInventory),
        TEXTURES
    );
  }

  private static net.minecraft.network.chat.Component getTooltip(boolean isPlayerInventory) {
    String key = isPlayerInventory ?
        "inventorymanagement.button.sort_player" :
        "inventorymanagement.button.sort_container";
    return net.minecraft.network.chat.Component.translatable(key);
  }
}
