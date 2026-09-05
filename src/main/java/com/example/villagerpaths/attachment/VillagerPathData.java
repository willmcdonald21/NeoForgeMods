package com.example.villagerpaths.attachment;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

public record VillagerPathData(List<BlockPos> waypoints, int currentIndex) {
    public static final VillagerPathData EMPTY = new VillagerPathData(List.of(), 0);

    public static final Codec<VillagerPathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("waypoints").forGetter(VillagerPathData::waypoints),
            Codec.INT.fieldOf("currentIndex").forGetter(VillagerPathData::currentIndex)
    ).apply(instance, VillagerPathData::new));

    public boolean hasPath() {
        return !waypoints.isEmpty();
    }

    public BlockPos currentWaypoint() {
        return waypoints.get(currentIndex);
    }

    public VillagerPathData advanced() {
        return new VillagerPathData(waypoints, (currentIndex + 1) % waypoints.size());
    }
}
