package me.roundaround.inventorymanagement.client;

import me.roundaround.inventorymanagement.client.gui.AutoStackButton;
import me.roundaround.inventorymanagement.client.gui.InventoryManagementButton;
import me.roundaround.inventorymanagement.client.gui.SortInventoryButton;
import me.roundaround.inventorymanagement.client.gui.TransferAllButton;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.inventory.InventoryHelper;
import me.roundaround.trove.client.gui.util.ScreenWidgets;
import me.roundaround.trove.config.value.Position;
import me.roundaround.trove.event.ScreenLifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class InventoryButtonsManager {
  public static final InventoryButtonsManager INSTANCE = new InventoryButtonsManager();

  private static final Minecraft MINECRAFT = Minecraft.getInstance();
  private static final int BUTTON_SPACING = 1;
  private static final int BUTTON_SHIFT_X = 0;
  private static final int BUTTON_SHIFT_Y = 1;

  private final LinkedHashSet<InventoryManagementButton> playerButtons = new LinkedHashSet<>();
  private final LinkedHashSet<InventoryManagementButton> containerButtons = new LinkedHashSet<>();
  private final HashSet<Class<? extends Container>> sortableInventories = new HashSet<>();
  private final HashSet<Class<? extends Container>> transferableInventories = new HashSet<>();
  private final HashSet<Class<? extends AbstractContainerMenu>> sortableScreenHandlers = new HashSet<>();
  private final HashSet<Class<? extends AbstractContainerMenu>> transferableScreenHandlers = new HashSet<>();

  private InventoryButtonsManager() {
    this.registerSortableContainer(Inventory.class);
    this.registerSortableContainer(PlayerEnderChestContainer.class);
    this.registerSortableContainer(RandomizableContainerBlockEntity.class);

    this.registerTransferableContainer(Inventory.class);
    this.registerTransferableContainer(PlayerEnderChestContainer.class);
    this.registerTransferableContainer(RandomizableContainerBlockEntity.class);

    this.registerSimpleInventorySortableHandler(ChestMenu.class);
    this.registerSimpleInventorySortableHandler(ShulkerBoxMenu.class);
    this.registerSimpleInventorySortableHandler(HorseInventoryMenu.class);
    this.registerSimpleInventorySortableHandler(HopperMenu.class);

    this.registerSimpleInventoryTransferableHandler(ChestMenu.class);
    this.registerSimpleInventoryTransferableHandler(ShulkerBoxMenu.class);
    this.registerSimpleInventoryTransferableHandler(HorseInventoryMenu.class);
  }

  public void registerSortableContainer(Class<? extends Container> clazz) {
    this.sortableInventories.add(clazz);
  }

  public void registerTransferableContainer(Class<? extends Container> clazz) {
    this.transferableInventories.add(clazz);
  }

  public void registerSimpleInventorySortableHandler(Class<? extends AbstractContainerMenu> clazz) {
    this.sortableScreenHandlers.add(clazz);
  }

  public void registerSimpleInventoryTransferableHandler(Class<? extends AbstractContainerMenu> clazz) {
    this.transferableScreenHandlers.add(clazz);
  }

  public void init() {
    ScreenLifecycle.afterInit(this::onScreenAfterInit);
  }

  private void onScreenAfterInit(Screen screen) {
    if (!(screen instanceof AbstractContainerScreen<?> handledScreen)) {
      return;
    }

    this.playerButtons.clear();
    this.containerButtons.clear();

    this.generateSortButton(handledScreen, false);
    this.generateAutoStackButton(handledScreen, false);
    this.generateTransferAllButton(handledScreen, false);

    this.generateSortButton(handledScreen, true);
    this.generateAutoStackButton(handledScreen, true);
    this.generateTransferAllButton(handledScreen, true);
  }

  private void generateSortButton(AbstractContainerScreen<?> screen, boolean isPlayerInventory) {
    if (!InventoryManagementConfig.getInstance().modEnabled.getValue() ||
        !InventoryManagementConfig.getInstance().showSort.getValue()) {
      return;
    }

    if (screen instanceof InventoryScreen && !isPlayerInventory) {
      return;
    }

    Slot referenceSlot = this.getReferenceSlot(screen, isPlayerInventory);
    if (referenceSlot == null) {
      return;
    }

    LocalPlayer player = MINECRAFT.player;
    if (player == null) {
      return;
    }

    Container inventory = isPlayerInventory ? player.getInventory() : InventoryHelper.getContainerInventory(player);
    if (inventory == null) {
      return;
    }

    if (inventory instanceof SimpleContainer) {
      if (this.sortableScreenHandlers.stream().noneMatch(clazz -> clazz.isInstance(screen.getMenu()))) {
        return;
      }
    } else {
      if (this.sortableInventories.stream().noneMatch(clazz -> clazz.isInstance(inventory))) {
        return;
      }
    }

    if (this.getNumberOfBulkInventorySlots(screen, isPlayerInventory) < 3) {
      return;
    }

    Position position = this.getButtonPosition(screen, isPlayerInventory);
    SortInventoryButton button = new SortInventoryButton(screen, inventory, referenceSlot, position, isPlayerInventory);
    this.addButton(screen, button, isPlayerInventory);
  }

  private void generateAutoStackButton(AbstractContainerScreen<?> screen, boolean isPlayerInventory) {
    if (!InventoryManagementConfig.getInstance().modEnabled.getValue() ||
        !InventoryManagementConfig.getInstance().showStack.getValue()) {
      return;
    }

    if (screen instanceof InventoryScreen && !isPlayerInventory) {
      return;
    }

    Slot referenceSlot = this.getReferenceSlot(screen, isPlayerInventory);
    if (referenceSlot == null) {
      return;
    }

    LocalPlayer player = MINECRAFT.player;
    if (player == null) {
      return;
    }

    Container fromInventory = isPlayerInventory ? InventoryHelper.getContainerInventory(player) : player.getInventory();
    Container toInventory = isPlayerInventory ? player.getInventory() : InventoryHelper.getContainerInventory(player);
    if (fromInventory == null || toInventory == null || fromInventory == toInventory) {
      return;
    }

    if (fromInventory instanceof SimpleContainer) {
      if (this.transferableScreenHandlers.stream().noneMatch(clazz -> clazz.isInstance(screen.getMenu()))) {
        return;
      }
    } else {
      if (this.transferableInventories.stream().noneMatch(clazz -> clazz.isInstance(fromInventory))) {
        return;
      }
    }

    if (toInventory instanceof SimpleContainer) {
      if (this.transferableScreenHandlers.stream().noneMatch(clazz -> clazz.isInstance(screen.getMenu()))) {
        return;
      }
    } else {
      if (this.transferableInventories.stream().noneMatch(clazz -> clazz.isInstance(toInventory))) {
        return;
      }
    }

    if (this.getNumberOfNonPlayerBulkInventorySlots(screen) < 3) {
      return;
    }

    Position position = this.getButtonPosition(screen, isPlayerInventory);
    AutoStackButton button = new AutoStackButton(screen, fromInventory, referenceSlot, position, isPlayerInventory);
    this.addButton(screen, button, isPlayerInventory);
  }

  private void generateTransferAllButton(AbstractContainerScreen<?> screen, boolean isPlayerInventory) {
    if (!InventoryManagementConfig.getInstance().modEnabled.getValue() ||
        !InventoryManagementConfig.getInstance().showTransfer.getValue()) {
      return;
    }

    if (screen instanceof InventoryScreen && !isPlayerInventory) {
      return;
    }

    Slot referenceSlot = this.getReferenceSlot(screen, isPlayerInventory);
    if (referenceSlot == null) {
      return;
    }

    LocalPlayer player = MINECRAFT.player;
    if (player == null) {
      return;
    }

    Container fromInventory = isPlayerInventory ? InventoryHelper.getContainerInventory(player) : player.getInventory();
    Container toInventory = isPlayerInventory ? player.getInventory() : InventoryHelper.getContainerInventory(player);
    if (fromInventory == null || toInventory == null || fromInventory == toInventory) {
      return;
    }

    if (fromInventory instanceof SimpleContainer) {
      if (this.transferableScreenHandlers.stream().noneMatch(clazz -> clazz.isInstance(screen.getMenu()))) {
        return;
      }
    } else {
      if (this.transferableInventories.stream().noneMatch(clazz -> clazz.isInstance(fromInventory))) {
        return;
      }
    }

    if (toInventory instanceof SimpleContainer) {
      if (this.transferableScreenHandlers.stream().noneMatch(clazz -> clazz.isInstance(screen.getMenu()))) {
        return;
      }
    } else {
      if (this.transferableInventories.stream().noneMatch(clazz -> clazz.isInstance(toInventory))) {
        return;
      }
    }

    if (this.getNumberOfNonPlayerBulkInventorySlots(screen) < 3) {
      return;
    }

    Position position = this.getButtonPosition(screen, isPlayerInventory);
    TransferAllButton button = new TransferAllButton(screen, fromInventory, referenceSlot, position, isPlayerInventory);
    this.addButton(screen, button, isPlayerInventory);
  }

  private void addButton(
      AbstractContainerScreen<?> screen,
      InventoryManagementButton button,
      boolean isPlayerInventory
  ) {
    ScreenWidgets.addWidget(screen, button);
    (isPlayerInventory ? this.playerButtons : this.containerButtons).add(button);
  }

  private Slot getReferenceSlot(AbstractContainerScreen<?> screen, boolean isPlayerInventory) {
    return screen.getMenu().slots.stream()
        .filter(slot -> isPlayerInventory == (slot.container instanceof Inventory))
        .max(Comparator.comparingInt(slot -> slot.x - slot.y))
        .orElse(null);
  }

  private int getNumberOfBulkInventorySlots(AbstractContainerScreen<?> screen, boolean isPlayerInventory) {
    return screen.getMenu().slots.stream()
        .filter(slot -> isPlayerInventory == (slot.container instanceof Inventory))
        .filter(slot -> !(screen.getMenu() instanceof HorseInventoryMenu) || slot.getContainerSlot() >= 2)
        .mapToInt(slot -> 1)
        .sum();
  }

  private int getNumberOfNonPlayerBulkInventorySlots(AbstractContainerScreen<?> screen) {
    return screen.getMenu().slots.stream()
        .filter(slot -> !(slot.container instanceof Inventory))
        .filter(slot -> !(screen.getMenu() instanceof HorseInventoryMenu) || slot.getContainerSlot() >= 2)
        .mapToInt(slot -> 1)
        .sum();
  }

  private Position getButtonPosition(Screen screen, boolean isPlayerInventory) {
    Position offset = InventoryManagementConfig.getInstance().screenPositions.get(screen, isPlayerInventory)
        .orElse(InventoryManagementConfig.getInstance().defaultPosition.getValue());
    return this.getButtonPosition((isPlayerInventory ? this.playerButtons : this.containerButtons).size(), offset);
  }

  public Position getButtonPosition(int index, Position offset) {
    int x = offset.x() + BUTTON_SHIFT_X * (InventoryManagementButton.WIDTH + BUTTON_SPACING) * index;
    int y = offset.y() + BUTTON_SHIFT_Y * (InventoryManagementButton.HEIGHT + BUTTON_SPACING) * index;

    return new Position(x, y);
  }

  public LinkedList<InventoryManagementButton> getPlayerButtons() {
    return new LinkedList<>(this.playerButtons);
  }

  public LinkedList<InventoryManagementButton> getContainerButtons() {
    return new LinkedList<>(this.containerButtons);
  }
}
