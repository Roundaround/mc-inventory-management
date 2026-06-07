package me.roundaround.inventorymanagement.client.gui.hud;

import me.roundaround.inventorymanagement.client.HotbarSwapClient;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class HotbarSwapHud {
  private static final int HOTBAR_HALF_WIDTH = 91;
  private static final int HOTBAR_HEIGHT = 22;

  // Vanilla draws the off-hand slot as a 29px-wide sprite flush against the
  // hotbar on the off-hand side (Gui#extractItemHotbar). When the off-hand holds
  // an item, the badge has to clear that slot, so it is pushed out by this much.
  private static final int OFFHAND_SLOT_WIDTH = 29;

  private static final int BADGE_WIDTH = 11;
  private static final int BADGE_HEIGHT = 13;
  private static final int BADGE_GAP = 3;
  private static final int BADGE_TEXT = 0xFFFFE066;

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
    Player player = minecraft.player;
    if (player == null) {
      return;
    }
    if (!Minecraft.renderNames()) {
      return;
    }

    int centerX = graphics.guiWidth() / 2;
    int hotbarTop = graphics.guiHeight() - HOTBAR_HEIGHT;
    int badgeTop = hotbarTop + (HOTBAR_HEIGHT - BADGE_HEIGHT) / 2;

    // The badge sits on the off-hand side of the hotbar, mirroring where vanilla
    // puts the off-hand slot: the left for a normal right-handed player, the
    // right when the player has selected left-handed (main arm LEFT) in Options.
    // When the off-hand is occupied, vanilla's 29px slot occupies that gap, so
    // reserve room and push the badge one slot further toward the screen edge.
    HumanoidArm offhandArm = player.getMainArm().getOpposite();
    int offhandReserve = player.getOffhandItem().isEmpty() ? 0 : OFFHAND_SLOT_WIDTH;

    int badgeLeft;
    if (offhandArm == HumanoidArm.LEFT) {
      badgeLeft = centerX - HOTBAR_HALF_WIDTH - offhandReserve - BADGE_GAP - BADGE_WIDTH;
    } else {
      badgeLeft = centerX + HOTBAR_HALF_WIDTH + offhandReserve + BADGE_GAP;
    }

    Font font = minecraft.font;
    String label = Integer.toString(row);
    int textX = badgeLeft + (BADGE_WIDTH - font.width(label)) / 2;
    int textY = badgeTop + (BADGE_HEIGHT - font.lineHeight) / 2 + 1;
    graphics.text(font, label, textX, textY, BADGE_TEXT, true);
  }
}
