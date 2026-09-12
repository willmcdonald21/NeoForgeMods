package com.example.villagerpaths.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** An axis-aligned rectangular zone (defined by two opposite corners) that a villager wanders within while lingering. */
public record Zone(BlockPos corner1, BlockPos corner2) {
    private static final double WALL_COVERAGE_THRESHOLD = 0.8;

    public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("corner1").forGetter(Zone::corner1),
            BlockPos.CODEC.fieldOf("corner2").forGetter(Zone::corner2)
    ).apply(instance, Zone::new));

    /**
     * Builds a zone from two clicked corners, insetting it by one block on every side
     * if the perimeter at that height is mostly solid (a built wall) - so the wander
     * area stays inside the wall instead of including the wall blocks themselves,
     * which pathfinding could otherwise resolve to just outside the enclosure.
     */
    public static Zone fromCorners(Level level, BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX());
        int maxX = Math.max(a.getX(), b.getX());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxZ = Math.max(a.getZ(), b.getZ());
        int y = a.getY();

        if (maxX - minX >= 2 && maxZ - minZ >= 2 && hasEnclosingWall(level, minX, maxX, minZ, maxZ, y)) {
            return new Zone(new BlockPos(minX + 1, y, minZ + 1), new BlockPos(maxX - 1, y, maxZ - 1));
        }
        return new Zone(a, b);
    }

    private static boolean hasEnclosingWall(Level level, int minX, int maxX, int minZ, int maxZ, int y) {
        int total = 0;
        int solid = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x != minX && x != maxX && z != minZ && z != maxZ) {
                    continue; // interior cell, not on the perimeter ring
                }
                total++;
                BlockPos wallPos = new BlockPos(x, y - 1, z);
                if (!level.getBlockState(wallPos).getCollisionShape(level, wallPos).isEmpty()) {
                    solid++;
                }
            }
        }
        return total > 0 && solid >= total * WALL_COVERAGE_THRESHOLD;
    }

    public BlockPos randomPointInside(RandomSource random) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        int x = minX + random.nextInt(maxX - minX + 1);
        int z = minZ + random.nextInt(maxZ - minZ + 1);
        return new BlockPos(x, corner1.getY(), z);
    }
}
