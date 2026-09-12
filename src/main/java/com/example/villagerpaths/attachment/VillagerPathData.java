package com.example.villagerpaths.attachment;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

/**
 * Per-villager progress along its assigned NamedPath: where it's currently headed
 * (a road tile or a point inside a zone), and - once arrived - how long it's busy
 * there before picking a new goal.
 */
public record VillagerPathData(
        Optional<UUID> pathId,
        Optional<BlockPos> currentGoal,
        int zoneIndex,
        long busyUntil,
        Optional<BlockPos> wanderTarget,
        long wanderPauseUntil
) {
    public static final VillagerPathData EMPTY =
            new VillagerPathData(Optional.empty(), Optional.empty(), -1, 0L, Optional.empty(), 0L);

    public static final Codec<VillagerPathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("pathId").forGetter(VillagerPathData::pathId),
            BlockPos.CODEC.optionalFieldOf("currentGoal").forGetter(VillagerPathData::currentGoal),
            Codec.INT.fieldOf("zoneIndex").forGetter(VillagerPathData::zoneIndex),
            Codec.LONG.fieldOf("busyUntil").forGetter(VillagerPathData::busyUntil),
            BlockPos.CODEC.optionalFieldOf("wanderTarget").forGetter(VillagerPathData::wanderTarget),
            Codec.LONG.fieldOf("wanderPauseUntil").forGetter(VillagerPathData::wanderPauseUntil)
    ).apply(instance, VillagerPathData::new));

    public static VillagerPathData assigned(UUID pathId) {
        return new VillagerPathData(Optional.of(pathId), Optional.empty(), -1, 0L, Optional.empty(), 0L);
    }

    public VillagerPathData travelingTo(BlockPos goal, int zoneIndex) {
        return new VillagerPathData(pathId, Optional.of(goal), zoneIndex, 0L, Optional.empty(), 0L);
    }

    public VillagerPathData arrivedAt(long busyUntilTime) {
        return new VillagerPathData(pathId, currentGoal, zoneIndex, busyUntilTime, Optional.empty(), 0L);
    }

    public VillagerPathData wanderingTowards(BlockPos target) {
        return new VillagerPathData(pathId, currentGoal, zoneIndex, busyUntil, Optional.of(target), 0L);
    }

    public VillagerPathData pausingUntil(long gameTime) {
        return new VillagerPathData(pathId, currentGoal, zoneIndex, busyUntil, wanderTarget, gameTime);
    }
}
