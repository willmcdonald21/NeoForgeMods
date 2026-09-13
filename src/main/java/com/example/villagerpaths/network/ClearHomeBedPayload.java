package com.example.villagerpaths.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: clear a villager's assigned home bed. */
public record ClearHomeBedPayload(int villagerEntityId) implements CustomPacketPayload {
    public static final Type<ClearHomeBedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "clear_home_bed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClearHomeBedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClearHomeBedPayload::villagerEntityId,
            ClearHomeBedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
