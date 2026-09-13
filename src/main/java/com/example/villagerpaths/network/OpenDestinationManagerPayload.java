package com.example.villagerpaths.network;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Server -> client: opens the Destination Manager screen with every named destination in the shared library. */
public record OpenDestinationManagerPayload(List<PathRef> allZones) implements CustomPacketPayload {
    public static final Type<OpenDestinationManagerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "open_destination_manager"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDestinationManagerPayload> STREAM_CODEC = StreamCodec.composite(
            PathRef.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenDestinationManagerPayload::allZones,
            OpenDestinationManagerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
