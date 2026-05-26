package me.roundaround.inventorymanagement.api.sorting;

import me.roundaround.inventorymanagement.config.InventoryManagementConfig;

/**
 * Populates {@link ItemVariantRegistry#COLOR} with every variant family defined in
 * {@link GroupDefs#ALL}, in declared order (which is the comparator's first-match-wins priority).
 *
 * <p>Lazy-safe: each group's enabled supplier defers its config read to comparison time, so
 * {@link #init()} may run before or after {@link InventoryManagementConfig#init()} with no ordering
 * hazard. The {@code done} flag guards against double-registration on any reload/re-entry path.
 */
public final class GroupBootstrap {
  private static boolean done = false;

  private GroupBootstrap() {
  }

  public static void init() {
    if (done) {
      return;
    }
    done = true;

    InventoryManagementConfig config = InventoryManagementConfig.getInstance();
    for (GroupDefs.GroupDef def : GroupDefs.ALL) {
      ItemVariantRegistry.COLOR.register(def.factory().apply(config.groupEnabled(def.id())));
    }
  }
}
