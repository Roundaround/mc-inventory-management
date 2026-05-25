package me.roundaround.inventorymanagement.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.roundaround.allay.api.Entrypoint;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.trove.client.gui.screen.ConfigScreen;

@Entrypoint(Entrypoint.MOD_MENU)
public class ModMenuImpl implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return (parent) -> new ConfigScreen(parent, Constants.MOD_ID, InventoryManagementConfig.getInstance());
  }
}
