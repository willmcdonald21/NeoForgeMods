package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: detach a destination from a path (the destination itself still exists). */
public record RemoveZoneFromPathPayload(UUID pathId, UUID zoneId) implements CustomPacketPayload {
    public static final Type<RemoveZoneFromPathPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "remove_zone_from_path"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveZoneFromPathPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RemoveZoneFromPathPayload::pathId,
            UUIDUtil.STREAM_CODEC, RemoveZoneFromPathPayload::zoneId,
            RemoveZoneFromPathPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
