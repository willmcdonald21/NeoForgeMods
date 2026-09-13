package com.example.villagerpaths.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: start a "pick a bed for this villager" request from the Villager Path screen. */
public record RequestHomeBedPayload(int villagerEntityId) implements CustomPacketPayload {
    public static final Type<RequestHomeBedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "request_home_bed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestHomeBedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RequestHomeBedPayload::villagerEntityId,
            RequestHomeBedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
