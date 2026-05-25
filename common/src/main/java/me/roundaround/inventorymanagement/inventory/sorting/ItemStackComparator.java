package me.roundaround.inventorymanagement.inventory.sorting;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SpectralArrowItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemStackComparator implements Comparator<ItemStack> {
  // TODO: Create more sorting algorithms & options:
  // Item/block categories
  // Place materials together
  // Allow registering reserve slots
  // More advanced configurations?

  private static final List<Comparator<ItemStack>> SUB_COMPARATORS = List.of(
      Comparator.comparing(ItemStackComparator::getSortName),
      ConditionalComparator.comparing(
          s -> s.get(DataComponents.TOOL) != null,
          SerialComparator.comparing(Comparator.comparingInt(ItemStackComparator::getMiningToolItemDamage).reversed(),
              Comparator.comparingInt(ItemStackComparator::getMiningToolItemSpeed).reversed()
          )
      ),
      ConditionalComparator.comparing(
          s -> s.get(DataComponents.EQUIPPABLE) != null,
          SerialComparator.comparing(Comparator.comparingInt(ItemStackComparator::getArmorSlot).reversed(),
              Comparator.comparingInt(ItemStackComparator::getArmorValue).reversed()
          )
      ),
      ConditionalComparator.comparing(
          ItemStackComparator::isPotion, SerialComparator.comparing(
              Comparator.comparing(ItemStackComparator::getPotionEffectName),
              Comparator.comparingInt(ItemStackComparator::getPotionLevel).reversed(),
              Comparator.comparingInt(ItemStackComparator::getPotionLength).reversed()
          )
      ),
      Comparator.comparingInt(ItemStackComparator::getHasNameAsInt).reversed(),
      ConditionalComparator.comparing(ItemStackComparator::hasCustomName,
          Comparator.comparing(s -> s.getHoverName().getString().toLowerCase(Locale.ROOT))
      ),
      Comparator.comparingInt(ItemStackComparator::getIsEnchantedAsInt).reversed(),
      ConditionalComparator.comparing(ItemStackComparator::isEnchantedBookOrEnchantedItem,
          Comparator.comparing(ItemStackComparator::getEnchantmentListAsString)
      ),
      Comparator.comparingInt(ItemStackComparator::getColor),
      Comparator.comparingInt(ItemStack::getCount).reversed(),
      Comparator.comparingInt(ItemStack::getDamageValue),
      Comparator.comparing(s -> s.getHoverName().getString().toLowerCase(Locale.ROOT))
  );

  private static final List<String> COMMON_SUFFIXES = List.of(
      "log",
      "wood",
      "leaves",
      "planks",
      "sign",
      "pressure_plate",
      "button",
      "door",
      "trapdoor",
      "fence",
      "fence_gate",
      "stairs",
      "ore",
      "boat",
      "spawn_egg",
      "soup",
      "seeds",
      "banner_pattern",
      "book",
      "map",
      "golden_apple",
      "minecart",
      "rail",
      "piston",
      "coral",
      "coral_wall_fan",
      "coral_block",
      "ice",
      "slab",
      "wall",
      "spawn_egg"
  );
  private static final List<String> COLOR_PREFIXES = Arrays.stream(DyeColor.values())
      .map(DyeColor::getName)
      .collect(Collectors.toList());
  private static final List<Tuple<String, String>> REGEX_REPLACERS = List.of(
      new Tuple<>("^stripped_(.+?)_(log|wood)$", "$2_stripped_$1"),
      new Tuple<>("^(.*?)concrete(?!_powder)(.*)$", "$1concrete_a$2"),
      new Tuple<>("^cooked_(.+)$", "$1_cooked"),
      new Tuple<>(String.format("^(.+?)_(%s)$", String.join("|", COMMON_SUFFIXES)), "$2_$1"),
      new Tuple<>(String.format("^(%s)_(.+)$", String.join("|", COLOR_PREFIXES)), "$2")
  );

  private final SerialComparator<ItemStack> underlyingComparator;

  private ItemStackComparator(SerialComparator<ItemStack> underlyingComparator) {
    this.underlyingComparator = underlyingComparator;
  }

  @Override
  public int compare(ItemStack o1, ItemStack o2) {
    return this.underlyingComparator.compare(o1, o2);
  }

  private static String getSortName(ItemStack itemStack) {
    Item item = itemStack.getItem();
    String itemString = item.toString();

    switch (item) {
      case LingeringPotionItem ignored -> {
        return "potion_lingering";
      }
      case SplashPotionItem ignored -> {
        return "potion_splash";
      }
      case PotionItem ignored -> {
        return "potion";
      }
      case SpectralArrowItem ignored -> {
        return "arrow_spectral";
      }
      case TippedArrowItem ignored -> {
        return "arrow_tipped";
      }
      case ArrowItem ignored -> {
        return "arrow";
      }
      case BlockItem blockItem -> {
        Block block = blockItem.getBlock();

        if (block instanceof FlowerBlock) {
          return "flower_" + itemString;
        }
      }
      default -> {
      }
    }

    for (Tuple<String, String> regexReplacer : REGEX_REPLACERS) {
      itemString = itemString.replaceAll(regexReplacer.getA(), regexReplacer.getB());
    }

    return itemString;
  }

  private static int getIsEnchantedAsInt(ItemStack itemStack) {
    return itemStack.isEnchanted() ? 1 : 0;
  }

  private static boolean isEnchantedBookOrEnchantedItem(ItemStack stack) {
    return stack.get(DataComponents.ENCHANTMENTS) != null ||
           stack.get(DataComponents.STORED_ENCHANTMENTS) != null;
  }

  // Only call when ^isEnchantedBookOrEnchantedItem
  private static String getEnchantmentListAsString(ItemStack stack) {
    ItemEnchantments component = Optional.ofNullable(stack.get(DataComponents.ENCHANTMENTS))
        .orElseGet(() -> stack.get(DataComponents.STORED_ENCHANTMENTS));
    if (component == null) {
      return "";
    }
    return component.keySet()
        .stream()
        .map((enchantment) -> Enchantment.getFullname(enchantment, component.getLevel(enchantment)))
        .map(Component::getString)
        .collect(Collectors.joining(" "));
  }

  // Only call when item has the TOOL component.
  private static int getMiningToolItemDamage(ItemStack itemStack) {
    return Optional.ofNullable(itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS))
        .orElse(ItemAttributeModifiers.EMPTY)
        .modifiers()
        .stream()
        .filter((modifier) -> modifier.matches(Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID))
        .findFirst()
        .map((entry) -> entry.modifier().amount() * 100f)
        .orElse(1d)
        .intValue();
  }

  // Only call when item has the TOOL component.
  private static int getMiningToolItemSpeed(ItemStack itemStack) {
    return Optional.ofNullable(itemStack.get(DataComponents.TOOL))
        .map((component) -> component.defaultMiningSpeed() * 100f)
        .orElse(1f)
        .intValue();
  }

  // Only call when item has the EQUIPPABLE component.
  private static int getArmorSlot(ItemStack itemStack) {
    return Optional.ofNullable(itemStack.get(DataComponents.EQUIPPABLE))
        .map(Equippable::slot)
        .map((slotType) -> {
          int groupValue = slotType.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? 10 : 0;
          return groupValue + slotType.getIndex();
        })
        .orElse(1);
  }

  // Only call when item has the EQUIPPABLE component.
  private static int getArmorValue(ItemStack itemStack) {
    return Optional.ofNullable(itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS))
        .orElse(ItemAttributeModifiers.EMPTY)
        .modifiers()
        .stream()
        .filter((modifier) -> {
          Identifier identifier = Identifier.withDefaultNamespace("armor." + ArmorType.BODY.getName());
          return modifier.matches(Attributes.ARMOR, identifier);
        })
        .findFirst()
        .map((entry) -> entry.modifier().amount() + 100f)
        .orElse(1d)
        .intValue();
  }

  private static boolean isPotion(ItemStack stack) {
    return stack.get(DataComponents.POTION_CONTENTS) != null;
  }

  private static String getPotionEffectName(ItemStack itemStack) {
    return streamPotionStatusEffects(itemStack).map(MobEffectInstance::getEffect)
        .map(Holder::value)
        .map(MobEffect::getDisplayName)
        .map(Component::getString)
        .min(Comparator.naturalOrder())
        .orElse("");
  }

  private static int getPotionLevel(ItemStack itemStack) {
    return streamPotionStatusEffects(itemStack).mapToInt(MobEffectInstance::getAmplifier).max().orElse(0);
  }

  private static int getPotionLength(ItemStack itemStack) {
    return streamPotionStatusEffects(itemStack).mapToInt(MobEffectInstance::getDuration).max().orElse(0);
  }

  private static Stream<MobEffectInstance> streamPotionStatusEffects(ItemStack stack) {
    return StreamSupport.stream(getPotionComponent(stack).getAllEffects().spliterator(), false);
  }

  private static PotionContents getPotionComponent(ItemStack stack) {
    return Optional.ofNullable(stack.get(DataComponents.POTION_CONTENTS)).orElse(PotionContents.EMPTY);
  }

  private static int getColor(ItemStack itemStack) {
    Item item = itemStack.getItem();

    DyedItemColor component = itemStack.get(DataComponents.DYED_COLOR);
    if (component != null) {
      return component.rgb();
    }

    String itemString = item.toString();

    return Arrays.stream(DyeColor.values())
        .filter(dyeColor -> itemString.startsWith(dyeColor.getName()))
        .mapToInt(DyeColor::getId)
        .map(i -> i + 1) // Colorless < all colors
        .findFirst()
        .orElse(0);
  }

  private static boolean hasCustomName(ItemStack stack) {
    return stack.get(DataComponents.CUSTOM_NAME) != null;
  }

  private static int getHasNameAsInt(ItemStack stack) {
    return hasCustomName(stack) ? 1 : 0;
  }

  public static ItemStackComparator comparator() {
    return new ItemStackComparator(SerialComparator.comparing(SUB_COMPARATORS));
  }
}
