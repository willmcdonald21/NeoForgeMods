package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: delete a named path entirely. */
public record DeletePathPayload(UUID pathId) implements CustomPacketPayload {
    public static final Type<DeletePathPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "delete_path"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DeletePathPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DeletePathPayload::pathId,
            DeletePathPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
