package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import me.roundaround.inventorymanagement.inventory.sorting.CachingComparatorImpl;
import me.roundaround.inventorymanagement.inventory.sorting.LexicographicalListComparator;
import me.roundaround.inventorymanagement.inventory.sorting.SerialComparator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.locale.Language;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantmentComparator extends CachingComparatorImpl<ItemStack, EnchantmentComparator.EnchantmentSummary> {
  private final DataComponentType<ItemEnchantments> type;

  public EnchantmentComparator(DataComponentType<ItemEnchantments> type) {
    super(Comparator.naturalOrder());
    this.type = type;
  }

  @Override
  protected EnchantmentSummary mapValue(ItemStack stack) {
    return EnchantmentSummary.of(stack, this.type);
  }

  protected record EnchantmentSummary(int count, int max, int sum, List<EnchantmentSortValue> mapped) implements
      Comparable<EnchantmentSummary> {
    private static Comparator<EnchantmentSummary> comparator;

    public static EnchantmentSummary of(ItemStack stack, DataComponentType<ItemEnchantments> type) {
      ItemEnchantments component = stack.get(type);
      if (component == null || component.isEmpty()) {
        return new EnchantmentSummary(0, 0, 0, List.of());
      }

      int count = component.size();
      int max = 0;
      int sum = 0;
      ArrayList<EnchantmentSortValue> mapped = new ArrayList<>(count);

      Language language = Language.getInstance();

      LinkedHashSet<Holder<Enchantment>> enchantments = component.keySet()
          .stream()
          .sorted(Comparator.comparing((entry) -> Enchantment.getFullname(entry, 1).getString()))
          .collect(Collectors.toCollection(LinkedHashSet::new));

      for (var entry : enchantments) {
        int level = component.getLevel(entry);
        max = Math.max(max, level);
        sum += level;
        mapped.add(new EnchantmentSortValue(Enchantment.getFullname(entry, 1).getString(), level));
      }

      return new EnchantmentSummary(count, max, sum, mapped);
    }

    @Override
    public int compareTo(@NotNull EnchantmentSummary other) {
      return getComparator().compare(this, other);
    }

    private static Comparator<EnchantmentSummary> getComparator() {
      if (comparator == null) {
        comparator = SerialComparator.comparing(
            Comparator.comparingInt(EnchantmentSummary::count).reversed(),
            Comparator.comparingInt(EnchantmentSummary::max).reversed(),
            Comparator.comparingInt(EnchantmentSummary::sum).reversed(),
            Comparator.comparing(EnchantmentSummary::mapped, LexicographicalListComparator.naturalOrder())
        );
      }
      return comparator;
    }
  }

  protected record EnchantmentSortValue(String name, int level) implements Comparable<EnchantmentSortValue> {
    @Override
    public int compareTo(@NotNull EnchantmentSortValue o) {
      int byName = this.name().compareTo(o.name());
      if (byName != 0) {
        return byName;
      }
      return o.level() - this.level();
    }
  }
}
