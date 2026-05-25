package me.roundaround.inventorymanagement.config;

import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.config.ConfigPath;
import me.roundaround.trove.config.manage.ModConfigImpl;
import me.roundaround.trove.config.manage.store.GameScopedFileStore;
import me.roundaround.trove.config.option.BooleanConfigOption;
import me.roundaround.trove.config.option.PositionConfigOption;
import me.roundaround.trove.config.value.Position;

public class InventoryManagementConfig extends ModConfigImpl implements GameScopedFileStore {
  private static InventoryManagementConfig instance;

  public BooleanConfigOption modEnabled;
  public BooleanConfigOption showSort;
  public BooleanConfigOption showTransfer;
  public BooleanConfigOption showStack;
  public PositionConfigOption defaultPosition;
  public PerScreenPositionConfigOption screenPositions;

  public InventoryManagementConfig() {
    super(Constants.MOD_ID);
  }

  public static InventoryManagementConfig getInstance() {
    if (instance == null) {
      instance = new InventoryManagementConfig();
    }
    return instance;
  }

  @Override
  protected void registerOptions() {
    this.modEnabled = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("modEnabled"))
        .setDefaultValue(true)
        .setComment("Simple toggle for the mod! Set to false to disable.")
        .build()).clientOnly().commit();

    this.showSort = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("showSort"))
        .setDefaultValue(true)
        .setComment("Whether to show sort buttons in the UI.")
        .build()).clientOnly().commit();

    this.showTransfer = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("showTransfer"))
        .setDefaultValue(true)
        .setComment("Whether to show transfer buttons in the UI.")
        .build()).clientOnly().commit();

    this.showStack = this.buildRegistration(BooleanConfigOption.builder(ConfigPath.of("showStack"))
        .setDefaultValue(true)
        .setComment("Whether to show auto-stack buttons in the UI.")
        .build()).clientOnly().commit();

    this.defaultPosition = this.buildRegistration(PositionConfigOption.builder(ConfigPath.of("defaultPosition"))
        .setDefaultValue(new Position(-4, -1))
        .setComment("Customize a default for button position.")
        .onUpdate((option) -> option.setDisabled(
            !this.showSort.getValue() && !this.showTransfer.getValue() && !this.showStack.getValue()))
        .build()).clientOnly().commit();

    this.screenPositions = this.buildRegistration(PerScreenPositionConfigOption.builder(ConfigPath.of(
        "screenPositions"))
        .setComment("Customize button position on a per-screen basis.")
        .build()).clientOnly().noGuiControl().commit();
  }
}
