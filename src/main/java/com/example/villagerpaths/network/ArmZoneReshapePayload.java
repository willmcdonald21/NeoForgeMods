package com.example.villagerpaths.network;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.example.villagerpaths.VillagerPathsMod;

/** Client -> server: arm a destination for reshaping - the sender's next two Destination Marker clicks redefine its corners. */
public record ArmZoneReshapePayload(UUID zoneId) implements CustomPacketPayload {
    public static final Type<ArmZoneReshapePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VillagerPathsMod.MODID, "arm_zone_reshape"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmZoneReshapePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ArmZoneReshapePayload::zoneId,
            ArmZoneReshapePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
