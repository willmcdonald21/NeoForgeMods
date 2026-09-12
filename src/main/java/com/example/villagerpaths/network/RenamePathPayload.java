package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: rename a shared named path. Affects every villager assigned to it. */
public record RenamePathPayload(UUID pathId, String newName) implements CustomPacketPayload {
    public static final Type<RenamePathPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "rename_path"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenamePathPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RenamePathPayload::pathId,
            ByteBufCodecs.STRING_UTF8, RenamePathPayload::newName,
            RenamePathPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
