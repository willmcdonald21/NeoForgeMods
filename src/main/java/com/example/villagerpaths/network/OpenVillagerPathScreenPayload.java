package com.example.villagerpaths.network;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Server -> client: tells the client to open the villager path screen with this data. */
public record OpenVillagerPathScreenPayload(int villagerEntityId, Optional<PathRef> assigned, List<PathRef> allPaths) implements CustomPacketPayload {
    public static final Type<OpenVillagerPathScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "open_villager_path_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenVillagerPathScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenVillagerPathScreenPayload::villagerEntityId,
            ByteBufCodecs.optional(PathRef.STREAM_CODEC), OpenVillagerPathScreenPayload::assigned,
            PathRef.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenVillagerPathScreenPayload::allPaths,
            OpenVillagerPathScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
