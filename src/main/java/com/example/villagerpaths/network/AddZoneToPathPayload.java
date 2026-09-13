package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: attach an existing destination to a path. */
public record AddZoneToPathPayload(UUID pathId, UUID zoneId) implements CustomPacketPayload {
    public static final Type<AddZoneToPathPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "add_zone_to_path"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddZoneToPathPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AddZoneToPathPayload::pathId,
            UUIDUtil.STREAM_CODEC, AddZoneToPathPayload::zoneId,
            AddZoneToPathPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
