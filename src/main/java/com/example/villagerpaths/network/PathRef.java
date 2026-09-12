package com.example.villagerpaths.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Lightweight id+name reference to a NamedPath, sent to the client for display/selection. */
public record PathRef(UUID id, String name) {
    public static final StreamCodec<ByteBuf, PathRef> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PathRef::id,
            ByteBufCodecs.STRING_UTF8, PathRef::name,
            PathRef::new
    );
}
