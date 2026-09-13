package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: delete a named destination entirely, removing it from every path that uses it. */
public record DeleteZonePayload(UUID zoneId) implements CustomPacketPayload {
    public static final Type<DeleteZonePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "delete_zone"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteZonePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DeleteZonePayload::zoneId,
            DeleteZonePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
