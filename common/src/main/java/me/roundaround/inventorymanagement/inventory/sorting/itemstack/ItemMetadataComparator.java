package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import me.roundaround.inventorymanagement.inventory.sorting.WrapperComparatorImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class ItemMetadataComparator extends WrapperComparatorImpl<ItemStack> {
  private static ItemMetadataComparator instance;

  private ItemMetadataComparator() {
    super(SerialComparator.comparing(
        new ToolComparator(),
        new ArmorComparator(),
        new CustomNameComparator(),
        new PlayerHeadNameComparator(),
        new EnchantmentComparator(DataComponents.ENCHANTMENTS),
        new EnchantmentComparator(DataComponents.STORED_ENCHANTMENTS),
        new ArmorTrimComparator(),
        new PaintingComparator(),
        new BannerComparator(),
        new FireworkRocketComparator(),
        new FireworkExplosionComparator(),
        new InstrumentComparator(),
        new MusicDiscComparator(),
        new PotionComparator(),
        new SuspiciousStewComparator(),
        new DecoratedPotComparator(),
        new WrittenBookComparator(),
        new ChargedProjectileComparator(),
        new BundleContentsComparator(),
        new OminousBottleComparator(),
        new MapIdComparator(),
        new DyedColorComparator(),
        new CountComparator(),
        new DamageComparator()
    ));
  }

  public static ItemMetadataComparator getInstance() {
    if (instance == null) {
      instance = new ItemMetadataComparator();
    }
    return instance;
  }
}
