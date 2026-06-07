package me.roundaround.inventorymanagement.client.gui.hud;

import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HotbarSwapHud {
  private static final int HOTBAR_HALF_WIDTH = 91;
  private static final int HOTBAR_HEIGHT = 22;

  private static final int BADGE_WIDTH = 11;
  private static final int BADGE_HEIGHT = 13;
  private static final int BADGE_GAP = 3;
  private static final int BADGE_BG = 0xC0000000;
  private static final int BADGE_BORDER = 0xFFFFD54A;
  private static final int BADGE_TEXT = 0xFFFFD54A;

  private HotbarSwapHud() {
  }

  public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
    int row = HotbarSwapClient.getSwappedRow();
    if (row == 0) {
      return;
    }

    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    if (!config.isInitialized() || !config.modEnabled.getValue()) {
      return;
    }

    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player == null) {
      return;
    }
    if (!Minecraft.renderNames()) {
      return;
    }

    int centerX = graphics.guiWidth() / 2;
    int hotbarLeft = centerX - HOTBAR_HALF_WIDTH;
    int hotbarTop = graphics.guiHeight() - HOTBAR_HEIGHT;

    int badgeRight = hotbarLeft - BADGE_GAP;
    int badgeLeft = badgeRight - BADGE_WIDTH;
    int badgeTop = hotbarTop + (HOTBAR_HEIGHT - BADGE_HEIGHT) / 2;

    graphics.fill(badgeLeft, badgeTop, badgeRight, badgeTop + BADGE_HEIGHT, BADGE_BG);
    graphics.outline(badgeLeft, badgeTop, BADGE_WIDTH, BADGE_HEIGHT, BADGE_BORDER);

    Font font = minecraft.font;
    String label = Integer.toString(row);
    int textX = badgeLeft + (BADGE_WIDTH - font.width(label)) / 2;
    int textY = badgeTop + (BADGE_HEIGHT - font.lineHeight) / 2 + 1;
    graphics.text(font, label, textX, textY, BADGE_TEXT, true);
  }
}
