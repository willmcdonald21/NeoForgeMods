package com.example.villagerpaths.attachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

/**
 * Per-villager progress along its assigned NamedPath: the remaining network-tile
 * hops left to walk before the final approach to its current goal (a road tile or
 * a point inside a zone), and - once arrived - how long it's busy there before
 * picking a new goal.
 */
public record VillagerPathData(
        Optional<UUID> pathId,
        Optional<BlockPos> currentGoal,
        List<BlockPos> route,
        int zoneIndex,
        long busyUntil,
        Optional<BlockPos> wanderTarget,
        long wanderPauseUntil
) {
    public static final VillagerPathData EMPTY =
            new VillagerPathData(Optional.empty(), Optional.empty(), List.of(), -1, 0L, Optional.empty(), 0L);

    public static final Codec<VillagerPathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("pathId").forGetter(VillagerPathData::pathId),
            BlockPos.CODEC.optionalFieldOf("currentGoal").forGetter(VillagerPathData::currentGoal),
            BlockPos.CODEC.listOf().optionalFieldOf("route", List.of()).forGetter(VillagerPathData::route),
            Codec.INT.fieldOf("zoneIndex").forGetter(VillagerPathData::zoneIndex),
            Codec.LONG.fieldOf("busyUntil").forGetter(VillagerPathData::busyUntil),
            BlockPos.CODEC.optionalFieldOf("wanderTarget").forGetter(VillagerPathData::wanderTarget),
            Codec.LONG.fieldOf("wanderPauseUntil").forGetter(VillagerPathData::wanderPauseUntil)
    ).apply(instance, VillagerPathData::new));

    public static VillagerPathData assigned(UUID pathId) {
        return new VillagerPathData(Optional.of(pathId), Optional.empty(), List.of(), -1, 0L, Optional.empty(), 0L);
    }

    public VillagerPathData travelingTo(BlockPos goal, int zoneIndex, List<BlockPos> route) {
        return new VillagerPathData(pathId, Optional.of(goal), route, zoneIndex, 0L, Optional.empty(), 0L);
    }

    public VillagerPathData advancedRoute() {
        List<BlockPos> remaining = route.isEmpty() ? route : route.subList(1, route.size());
        return new VillagerPathData(pathId, currentGoal, List.copyOf(remaining), zoneIndex, busyUntil, wanderTarget, wanderPauseUntil);
    }

    public VillagerPathData arrivedAt(long busyUntilTime) {
        return new VillagerPathData(pathId, currentGoal, List.of(), zoneIndex, busyUntilTime, Optional.empty(), 0L);
    }

    public VillagerPathData wanderingTowards(BlockPos target) {
        return new VillagerPathData(pathId, currentGoal, route, zoneIndex, busyUntil, Optional.of(target), 0L);
    }

    public VillagerPathData pausingUntil(long gameTime) {
        return new VillagerPathData(pathId, currentGoal, route, zoneIndex, busyUntil, wanderTarget, gameTime);
    }
}
