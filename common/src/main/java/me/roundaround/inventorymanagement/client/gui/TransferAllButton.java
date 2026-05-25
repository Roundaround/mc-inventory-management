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

public class TransferAllButton extends InventoryManagementButton {
  private static final WidgetSprites TEXTURES_FROM = new WidgetSprites(
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "transfer_from"),
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "transfer_from_highlighted")
  );
  private static final WidgetSprites TEXTURES_TO = new WidgetSprites(
      Identifier.fromNamespaceAndPath(
          Constants.MOD_ID,
          "transfer_to"
      ),
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "transfer_to_highlighted")
  );

  public TransferAllButton(
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
        (button) -> ClientNetworking.sendTransfer(fromPlayerInventory),
        getTooltip(fromPlayerInventory),
        fromPlayerInventory ? TEXTURES_TO : TEXTURES_FROM
    );
  }

  public TransferAllButton(
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
        (button) -> ClientNetworking.sendTransfer(fromPlayerInventory),
        getTooltip(fromPlayerInventory),
        fromPlayerInventory ? TEXTURES_TO : TEXTURES_FROM
    );
  }

  private static net.minecraft.network.chat.Component getTooltip(boolean fromPlayerInventory) {
    String key = fromPlayerInventory ?
        "inventorymanagement.button.transfer_place" :
        "inventorymanagement.button.transfer_take";
    return net.minecraft.network.chat.Component.translatable(key);
  }
}
