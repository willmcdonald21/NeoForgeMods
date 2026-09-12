package com.example.villagerpaths.attachment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Flood-fills outward from a seed point across every horizontally-connected tile whose surface block matches a palette. */
public final class NetworkFloodFill {
    private static final int MAX_TILES = 2000;
    private static final int SEED_SEARCH_RADIUS = 5;

    private NetworkFloodFill() {
    }

    public static List<BlockPos> fill(Level level, BlockPos start, Set<Block> palette) {
        if (palette.isEmpty()) {
            return List.of();
        }
        BlockPos seed = findSeed(level, start, palette);
        if (seed == null) {
            return List.of();
        }

        List<BlockPos> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);
        visited.add(seed.asLong());

        while (!queue.isEmpty() && result.size() < MAX_TILES) {
            BlockPos current = queue.poll();
            result.add(current);
            for (BlockPos neighbor : horizontalNeighbors(level, current, palette)) {
                if (visited.add(neighbor.asLong())) {
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }

    private static List<BlockPos> horizontalNeighbors(Level level, BlockPos from, Set<Block> palette) {
        List<BlockPos> found = new ArrayList<>(4);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            for (int dy = 1; dy >= -1; dy--) {
                BlockPos candidate = from.offset(dir[0], dy, dir[1]);
                if (matches(level, candidate, palette)) {
                    found.add(candidate);
                    break;
                }
            }
        }
        return found;
    }

    private static BlockPos findSeed(Level level, BlockPos start, Set<Block> palette) {
        if (matches(level, start, palette)) {
            return start;
        }
        for (int r = 1; r <= SEED_SEARCH_RADIUS; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        BlockPos candidate = start.offset(dx, dy, dz);
                        if (matches(level, candidate, palette)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean matches(Level level, BlockPos point, Set<Block> palette) {
        return palette.contains(level.getBlockState(point.below()).getBlock());
    }
}
