package com.example.villagerpaths.network;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Server -> client: opens the Path Manager screen with every named path (and its zones) and the full zone library. */
public record OpenPathManagerPayload(List<PathWithZones> paths, List<PathRef> allZones) implements CustomPacketPayload {
    public static final Type<OpenPathManagerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "open_path_manager"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPathManagerPayload> STREAM_CODEC = StreamCodec.composite(
            PathWithZones.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenPathManagerPayload::paths,
            PathRef.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenPathManagerPayload::allZones,
            OpenPathManagerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
