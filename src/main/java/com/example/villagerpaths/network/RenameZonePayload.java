package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: rename a shared destination. Affects every path that uses it. */
public record RenameZonePayload(UUID zoneId, String newName) implements CustomPacketPayload {
    public static final Type<RenameZonePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "rename_zone"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenameZonePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RenameZonePayload::zoneId,
            ByteBufCodecs.STRING_UTF8, RenameZonePayload::newName,
            RenameZonePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
