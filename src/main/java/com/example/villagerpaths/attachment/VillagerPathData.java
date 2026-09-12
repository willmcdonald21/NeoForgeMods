package com.example.villagerpaths.attachment;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

public record VillagerPathData(List<PathStep> steps, int currentIndex, long lingerUntil, Optional<BlockPos> wanderTarget) {
    public static final VillagerPathData EMPTY = new VillagerPathData(List.of(), 0, 0L, Optional.empty());

    public static final Codec<VillagerPathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PathStep.CODEC.listOf().fieldOf("steps").forGetter(VillagerPathData::steps),
            Codec.INT.fieldOf("currentIndex").forGetter(VillagerPathData::currentIndex),
            Codec.LONG.fieldOf("lingerUntil").forGetter(VillagerPathData::lingerUntil),
            BlockPos.CODEC.optionalFieldOf("wanderTarget").forGetter(VillagerPathData::wanderTarget)
    ).apply(instance, VillagerPathData::new));

    public boolean hasPath() {
        return !steps.isEmpty();
    }

    public PathStep currentStep() {
        return steps.get(currentIndex);
    }

    public VillagerPathData advanced() {
        return new VillagerPathData(steps, (currentIndex + 1) % steps.size(), 0L, Optional.empty());
    }

    public VillagerPathData lingeringUntil(long gameTime, BlockPos initialWanderTarget) {
        return new VillagerPathData(steps, currentIndex, gameTime, Optional.of(initialWanderTarget));
    }

    public VillagerPathData withWanderTarget(BlockPos target) {
        return new VillagerPathData(steps, currentIndex, lingerUntil, Optional.of(target));
    }
}
