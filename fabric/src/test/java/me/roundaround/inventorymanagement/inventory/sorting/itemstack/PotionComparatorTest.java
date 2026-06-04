package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.createListOfEmpty;
import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PotionComparatorTest extends BaseMinecraftTest {
  private static final String PREFIX = "item.minecraft.potion.effect.";

  private static PotionComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new PotionComparator();
  }

  // Directly answers the reported worry: PotionContents.getName(prefix).getString() must resolve the
  // translatable component through the loaded Language, NOT echo back the raw i18n key. If this ever
  // regressed to key-echo, the comparator would silently sort by key instead of by display name.
  @Test
  void translationResolvedNotKeyEchoed() {
    String resolved = new PotionContents(Potions.HEALING).getName(PREFIX).getString();

    assertEquals("Potion of Healing", resolved, "getName(prefix).getString() should resolve the display name");
    assertNotEquals(PREFIX + "healing", resolved, "getName(prefix).getString() should not echo the raw i18n key");
  }

  @ParameterizedTest
  @MethodSource("getNonPotionSamples")
  void ignoresItemsWithoutComponent(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getNonPotionSamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.POTION_CONTENTS,
        Items.DIAMOND,
        Items.DIRT,
        Items.POTION
    ));
  }

  // Primary key is the resolved display name, not the potion id/key. Display-name alphabetical order
  // diverges from key order (e.g. "Water Bottle" sorts last despite its "water" key sorting late too,
  // while "harming"/"healing" only resolve to a stable order once translated), so this passes only if
  // the comparator's primary key is the translated name.
  @Test
  void sortsByTranslatedNameAscending() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createPotion(Potions.HARMING),       // Potion of Harming
        createPotion(Potions.HEALING),       // Potion of Healing
        createPotion(Potions.INVISIBILITY),  // Potion of Invisibility
        createPotion(Potions.LEAPING),       // Potion of Leaping
        createPotion(Potions.POISON),        // Potion of Poison
        createPotion(Potions.STRENGTH),      // Potion of Strength
        createPotion(Potions.SWIFTNESS),     // Potion of Swiftness
        createPotion(Potions.WATER)          // Water Bottle
    ));
  }

  @Test
  void samePotionIsEqual() {
    assertEquals(0, comparator.compare(createPotion(Potions.HEALING), createPotion(Potions.HEALING)));
  }

  // Secondary tie-break: when the translated name is identical (same customName suffix here), the stack
  // with MORE custom effects sorts first (customEffects().size() is compared in reverse order).
  @Test
  void moreCustomEffectsSortFirst() {
    ItemStack fewer = createCustomPotion("x", new MobEffectInstance(MobEffects.STRENGTH, 100));
    ItemStack more = createCustomPotion("x",
        new MobEffectInstance(MobEffects.STRENGTH, 100),
        new MobEffectInstance(MobEffects.SPEED, 100));

    assertTrue(comparator.compare(more, fewer) < 0, "More custom effects should sort first");
    assertTrue(comparator.compare(fewer, more) > 0, "Fewer custom effects should sort last");
  }

  // Final tie-break: identical name, equal effect count, and equal (empty) effect lists fall through to
  // customColor, ordered nulls-last by natural order.
  @Test
  void customColorTieBreak() {
    assertTrue(comparator.compare(
        createCustomColorPotion(Potions.WATER, 0x000000),
        createCustomColorPotion(Potions.WATER, 0xFFFFFF)
    ) < 0, "Lower custom color should sort first");
    assertTrue(comparator.compare(
        createCustomColorPotion(Potions.WATER, 0x000000),
        createCustomColorPotion(Potions.WATER, null)
    ) < 0, "A present custom color should sort before a missing one (nulls last)");
    assertTrue(comparator.compare(
        createCustomColorPotion(Potions.WATER, null),
        createCustomColorPotion(Potions.WATER, 0x000000)
    ) > 0, "A missing custom color should sort after a present one (nulls last)");
  }

  private static ItemStack createPotion(Holder<Potion> potion) {
    ItemStack stack = new ItemStack(Items.POTION);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return stack;
  }

  private static ItemStack createCustomPotion(String customName, MobEffectInstance... customEffects) {
    ItemStack stack = new ItemStack(Items.POTION);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
        Optional.empty(),
        Optional.empty(),
        List.of(customEffects),
        Optional.of(customName)
    ));
    return stack;
  }

  private static ItemStack createCustomColorPotion(Holder<Potion> base, Integer color) {
    ItemStack stack = new ItemStack(Items.POTION);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
        Optional.of(base),
        Optional.ofNullable(color),
        List.of(),
        Optional.empty()
    ));
    return stack;
  }
}
