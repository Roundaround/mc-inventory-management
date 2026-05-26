package me.roundaround.inventorymanagement.network;

import me.roundaround.inventorymanagement.generated.Constants;
import me.roundaround.inventorymanagement.server.inventory.ServerInventoryHelper;
import me.roundaround.trove.network.TroveNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class Networking {
  private Networking() {
  }

  public static final Identifier STACK_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stack_c2s");
  public static final Identifier SORT_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sort_c2s");
  public static final Identifier SORT_ALL_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sort_all_c2s");
  public static final Identifier TRANSFER_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "transfer_c2s");

  private static final StreamCodec<ByteBuf, List<Integer>> INT_LIST_CODEC =
      ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list());

  public static void register() {
    TroveNetworking.registerC2S(StackC2S.ID, StackC2S.CODEC,
        (payload, player) -> ServerInventoryHelper.autoStack(player, payload.fromPlayerInventory()));
    TroveNetworking.registerC2S(SortC2S.ID, SortC2S.CODEC,
        (payload, player) -> ServerInventoryHelper.applySort(player, payload.isPlayerInventory(), payload.sorted()));
    TroveNetworking.registerC2S(SortAllC2S.ID, SortAllC2S.CODEC,
        (payload, player) -> {
          ServerInventoryHelper.applySort(player, true, payload.playerSorted());
          ServerInventoryHelper.applySort(player, false, payload.containerSorted());
        });
    TroveNetworking.registerC2S(TransferC2S.ID, TransferC2S.CODEC,
        (payload, player) -> ServerInventoryHelper.transferAll(player, payload.fromPlayerInventory()));
  }

  public record StackC2S(boolean fromPlayerInventory) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StackC2S> ID = new CustomPacketPayload.Type<>(STACK_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, StackC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, StackC2S::fromPlayerInventory, StackC2S::new);

    @Override @NotNull public Type<? extends CustomPacketPayload> type() { return ID; }
  }

  public record SortC2S(boolean isPlayerInventory, List<Integer> sorted) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SortC2S> ID = new CustomPacketPayload.Type<>(SORT_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SortC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, SortC2S::isPlayerInventory,
        INT_LIST_CODEC, SortC2S::sorted,
        SortC2S::new);

    @Override @NotNull public Type<? extends CustomPacketPayload> type() { return ID; }
  }

  public record SortAllC2S(List<Integer> playerSorted, List<Integer> containerSorted) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SortAllC2S> ID = new CustomPacketPayload.Type<>(SORT_ALL_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SortAllC2S> CODEC = StreamCodec.composite(
        INT_LIST_CODEC, SortAllC2S::playerSorted,
        INT_LIST_CODEC, SortAllC2S::containerSorted,
        SortAllC2S::new);

    @Override @NotNull public Type<? extends CustomPacketPayload> type() { return ID; }
  }

  public record TransferC2S(boolean fromPlayerInventory) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TransferC2S> ID = new CustomPacketPayload.Type<>(TRANSFER_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, TransferC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, TransferC2S::fromPlayerInventory, TransferC2S::new);

    @Override @NotNull public Type<? extends CustomPacketPayload> type() { return ID; }
  }
}
