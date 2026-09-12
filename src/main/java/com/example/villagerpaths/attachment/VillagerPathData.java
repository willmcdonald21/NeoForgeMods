package com.example.villagerpaths.attachment;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

/** Per-villager progress along whichever NamedPath it's assigned (looked up from the PathLibrary by id). */
public record VillagerPathData(
        Optional<UUID> pathId,
        int currentIndex,
        long lingerUntil,
        Optional<BlockPos> wanderTarget,
        long wanderPauseUntil
) {
    public static final VillagerPathData EMPTY = new VillagerPathData(Optional.empty(), 0, 0L, Optional.empty(), 0L);

    public static final Codec<VillagerPathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("pathId").forGetter(VillagerPathData::pathId),
            Codec.INT.fieldOf("currentIndex").forGetter(VillagerPathData::currentIndex),
            Codec.LONG.fieldOf("lingerUntil").forGetter(VillagerPathData::lingerUntil),
            BlockPos.CODEC.optionalFieldOf("wanderTarget").forGetter(VillagerPathData::wanderTarget),
            Codec.LONG.fieldOf("wanderPauseUntil").forGetter(VillagerPathData::wanderPauseUntil)
    ).apply(instance, VillagerPathData::new));

    public static VillagerPathData assigned(UUID pathId) {
        return new VillagerPathData(Optional.of(pathId), 0, 0L, Optional.empty(), 0L);
    }

    public VillagerPathData advanced(int stepCount) {
        return new VillagerPathData(pathId, (currentIndex + 1) % stepCount, 0L, Optional.empty(), 0L);
    }

    public VillagerPathData lingeringUntil(long gameTime) {
        return new VillagerPathData(pathId, currentIndex, gameTime, Optional.empty(), 0L);
    }

    public VillagerPathData wanderingTowards(BlockPos target) {
        return new VillagerPathData(pathId, currentIndex, lingerUntil, Optional.of(target), 0L);
    }

    public VillagerPathData pausingUntil(long gameTime) {
        return new VillagerPathData(pathId, currentIndex, lingerUntil, wanderTarget, gameTime);
    }
}
