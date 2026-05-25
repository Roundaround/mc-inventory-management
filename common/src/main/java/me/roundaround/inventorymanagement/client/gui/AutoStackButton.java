package me.roundaround.inventorymanagement.client.gui;

import me.roundaround.inventorymanagement.client.gui.screen.ScreenPositioner;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.config.value.Position;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class AutoStackButton extends InventoryManagementButton {
  private static final WidgetSprites TEXTURES_FROM = new WidgetSprites(
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stack_from"),
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stack_from_highlighted")
  );
  private static final WidgetSprites TEXTURES_TO = new WidgetSprites(
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stack_to"),
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stack_to_highlighted")
  );

  public AutoStackButton(
      AbstractContainerScreen<?> parent,
      Container inventory,
      Slot referenceSlot,
      Position offset,
      boolean fromPlayerInventory
  ) {
    super(
        parent,
        inventory,
        referenceSlot,
        offset,
        fromPlayerInventory,
        (button) -> ClientNetworking.sendStack(fromPlayerInventory),
        getTooltip(fromPlayerInventory),
        fromPlayerInventory ? TEXTURES_TO : TEXTURES_FROM
    );
  }

  public AutoStackButton(
      ScreenPositioner parent,
      Container inventory,
      Slot referenceSlot,
      Position offset,
      boolean fromPlayerInventory
  ) {
    super(
        parent,
        inventory,
        referenceSlot,
        offset,
        fromPlayerInventory,
        (button) -> ClientNetworking.sendStack(fromPlayerInventory),
        getTooltip(fromPlayerInventory),
        fromPlayerInventory ? TEXTURES_TO : TEXTURES_FROM
    );
  }

  private static Component getTooltip(boolean fromPlayerInventory) {
    String key = fromPlayerInventory ?
        "inventorymanagement.button.autostack_into" :
        "inventorymanagement.button.autostack_from";
    return Component.translatable(key);
  }
}
