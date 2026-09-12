package com.example.villagerpaths.attachment;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * An axis-aligned rectangular zone (defined by two opposite corners) that a villager
 * wanders within while lingering. Optionally restricted to a palette of surface
 * block types - if non-empty, only points standing on one of those blocks are valid.
 */
public record Zone(BlockPos corner1, BlockPos corner2, List<ResourceLocation> allowedSurfaces) {
    private static final double WALL_COVERAGE_THRESHOLD = 0.8;
    private static final int SURFACE_SEARCH_ATTEMPTS = 20;

    public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("corner1").forGetter(Zone::corner1),
            BlockPos.CODEC.fieldOf("corner2").forGetter(Zone::corner2),
            ResourceLocation.CODEC.listOf().optionalFieldOf("allowedSurfaces", List.of()).forGetter(Zone::allowedSurfaces)
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
            return new Zone(new BlockPos(minX + 1, y, minZ + 1), new BlockPos(maxX - 1, y, maxZ - 1), List.of());
        }
        return new Zone(a, b, List.of());
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

    public Zone withAllowedSurfaces(List<ResourceLocation> surfaces) {
        return new Zone(corner1, corner2, List.copyOf(surfaces));
    }

    /**
     * Picks a random point in the zone. If a surface palette is set, only a point whose
     * block below matches the palette is returned; if none is found after a bounded
     * number of tries, empty is returned (caller should have the villager stand still).
     */
    public Optional<BlockPos> randomPointInside(RandomSource random, LevelReader level) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());

        int attempts = allowedSurfaces.isEmpty() ? 1 : SURFACE_SEARCH_ATTEMPTS;
        for (int i = 0; i < attempts; i++) {
            int x = minX + random.nextInt(maxX - minX + 1);
            int z = minZ + random.nextInt(maxZ - minZ + 1);
            BlockPos candidate = new BlockPos(x, corner1.getY(), z);
            if (allowedSurfaces.isEmpty() || matchesPalette(level, candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private boolean matchesPalette(LevelReader level, BlockPos point) {
        BlockPos below = point.below();
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(below).getBlock());
        return allowedSurfaces.contains(key);
    }
}
