package com.example.villagerpaths.network;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** A path's id/name plus the destinations it currently uses, for the Path Manager screen. */
public record PathWithZones(UUID id, String name, List<PathRef> zones) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PathWithZones> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PathWithZones::id,
            ByteBufCodecs.STRING_UTF8, PathWithZones::name,
            PathRef.STREAM_CODEC.apply(ByteBufCodecs.list()), PathWithZones::zones,
            PathWithZones::new
    );
}
