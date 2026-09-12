package com.example.villagerpaths.attachment;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

public record VillagerHomeData(Optional<BlockPos> bed) {
    public static final VillagerHomeData EMPTY = new VillagerHomeData(Optional.empty());

    public static final Codec<VillagerHomeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.optionalFieldOf("bed").forGetter(VillagerHomeData::bed)
    ).apply(instance, VillagerHomeData::new));
}
