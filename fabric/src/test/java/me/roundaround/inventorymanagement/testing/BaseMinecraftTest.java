package me.roundaround.inventorymanagement.testing;

import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class BaseMinecraftTest {
  protected static final UUID PLAYER_UUID = UUID.randomUUID();

  @BeforeAll
  static void bootstrapMinecraft() {
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();
    bindDataComponents();
  }

  private static void bindDataComponents() {
    HolderLookup.Provider provider = createEmptyTagProvider();

    try {
      BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(provider)
          .forEach(DataComponentInitializers.PendingComponents::apply);
    } catch (Exception e) {
      for (var registry : BuiltInRegistries.REGISTRY) {
        registry.listElements().forEach(holder -> {
          try {
            holder.components();
          } catch (NullPointerException npe) {
            DataComponentMap defaults = DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build();
            holder.bindComponents(defaults);
          }
        });
      }
    }
  }

  private static HolderLookup.Provider createEmptyTagProvider() {
    return HolderLookup.Provider.create(BuiltInRegistries.REGISTRY.stream().map(EmptyTagRegistryLookup::new));
  }

  @SuppressWarnings("unchecked")
  private static class EmptyTagRegistryLookup<T> implements HolderLookup.RegistryLookup<T> {
    private final Registry<T> delegate;

    EmptyTagRegistryLookup(Registry<?> registry) {
      this.delegate = (Registry<T>) registry;
    }

    @Override
    @NotNull
    public ResourceKey<? extends Registry<? extends T>> key() {
      return this.delegate.key();
    }

    @Override
    @NotNull
    public Optional<Holder.Reference<T>> get(@NotNull ResourceKey<T> key) {
      return this.delegate.get(key);
    }

    @Override
    @NotNull
    public Stream<Holder.Reference<T>> listElements() {
      return this.delegate.listElements();
    }

    @Override
    @NotNull
    public Optional<HolderSet.Named<T>> get(@NotNull TagKey<T> tagKey) {
      return Optional.of(HolderSet.emptyNamed(this.delegate, tagKey));
    }

    @Override
    @NotNull
    public Stream<HolderSet.Named<T>> listTags() {
      return Stream.empty();
    }

    @Override
    @NotNull
    public Lifecycle registryLifecycle() {
      return Lifecycle.stable();
    }

    @Override
    @NotNull
    public Holder.Reference<T> getOrThrow(@NotNull ResourceKey<T> key) {
      return this.delegate.get(key)
          .orElseThrow(() -> new IllegalStateException("Missing key in registry " + this.delegate.key() + ": " + key));
    }
  }
}
