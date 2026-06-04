package me.roundaround.inventorymanagement.inventory.sorting.itemstack;

import com.google.common.collect.Lists;
import me.roundaround.inventorymanagement.testing.BaseMinecraftTest;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static me.roundaround.inventorymanagement.testing.DataGen.createListOfEmpty;
import static me.roundaround.inventorymanagement.testing.DataGen.getUniquePairs;
import static me.roundaround.inventorymanagement.testing.IterableMatchHelpers.assertPreservesOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuspiciousStewComparatorTest extends BaseMinecraftTest {
  private static SuspiciousStewComparator comparator;

  @BeforeAll
  static void beforeAll() {
    comparator = new SuspiciousStewComparator();
  }

  @ParameterizedTest
  @MethodSource("getEmptySamples")
  void ignoresItemsWithoutComponent(ItemStack a, ItemStack b) {
    assertEquals(0, comparator.compare(a, b));
  }

  private static Stream<Arguments> getEmptySamples() {
    return getUniquePairs(createListOfEmpty(
        DataComponents.SUSPICIOUS_STEW_EFFECTS,
        Items.SUSPICIOUS_STEW,
        Items.DIAMOND,
        Items.DIRT
    ));
  }

  @Test
  void moreEffectsSortFirst() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createStew(
            entry(MobEffects.SPEED, 200),
            entry(MobEffects.STRENGTH, 200),
            entry(MobEffects.POISON, 200)
        ),
        createStew(
            entry(MobEffects.SPEED, 200),
            entry(MobEffects.STRENGTH, 200)
        ),
        createStew(
            entry(MobEffects.SPEED, 200)
        )
    ));
  }

  @Test
  void nameOrderingTieBreak() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createStew(entry(MobEffects.HASTE, 200)),
        createStew(entry(MobEffects.SPEED, 200)),
        createStew(entry(MobEffects.STRENGTH, 200))
    ));
  }

  @Test
  void durationDescTieBreakForSameName() {
    assertPreservesOrder(comparator, Lists.newArrayList(
        createStew(entry(MobEffects.SPEED, 400)),
        createStew(entry(MobEffects.SPEED, 200)),
        createStew(entry(MobEffects.SPEED, 100))
    ));
  }

  @Test
  void sameEffectsAreEqual() {
    ItemStack a = createStew(entry(MobEffects.SPEED, 200));
    ItemStack b = createStew(entry(MobEffects.SPEED, 200));
    assertEquals(0, comparator.compare(a, b));
  }

  @Test
  void emptyStewsAreEqual() {
    assertEquals(0, comparator.compare(createStew(), createStew()));
  }

  private static ItemStack createStew(SuspiciousStewEffects.Entry... entries) {
    ItemStack stack = new ItemStack(Items.SUSPICIOUS_STEW);
    stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(List.of(entries)));
    return stack;
  }

  private static SuspiciousStewEffects.Entry entry(Holder<MobEffect> effect, int duration) {
    return new SuspiciousStewEffects.Entry(effect, duration);
  }
}
