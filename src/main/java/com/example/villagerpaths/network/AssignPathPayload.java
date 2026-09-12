package com.example.villagerpaths.network;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: assign (or clear, if pathId is empty) a villager's path. */
public record AssignPathPayload(int villagerEntityId, Optional<UUID> pathId) implements CustomPacketPayload {
    public static final Type<AssignPathPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "assign_path"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AssignPathPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AssignPathPayload::villagerEntityId,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AssignPathPayload::pathId,
            AssignPathPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
