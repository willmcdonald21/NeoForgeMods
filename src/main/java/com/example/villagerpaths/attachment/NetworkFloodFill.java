package com.example.villagerpaths.attachment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    /** Finds the tile in the network closest to the given position (straight-line distance). */
    public static BlockPos nearestTile(List<BlockPos> tiles, BlockPos from) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos tile : tiles) {
            double dist = tile.distSqr(from);
            if (dist < bestDist) {
                bestDist = dist;
                best = tile;
            }
        }
        return best;
    }

    /**
     * Finds the shortest hop-by-hop path from one network tile to another, stepping only
     * across tiles that are actually in the network - never off it. Returns an empty list
     * if either endpoint isn't a network tile or they're not connected.
     */
    public static List<BlockPos> route(List<BlockPos> tiles, BlockPos from, BlockPos to) {
        Set<Long> tileSet = new HashSet<>();
        for (BlockPos tile : tiles) {
            tileSet.add(tile.asLong());
        }
        if (!tileSet.contains(from.asLong()) || !tileSet.contains(to.asLong())) {
            return List.of();
        }

        Map<Long, BlockPos> cameFrom = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(from);
        cameFrom.put(from.asLong(), null);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current.equals(to)) {
                break;
            }
            for (BlockPos neighbor : geometricNeighbors(current, tileSet)) {
                long key = neighbor.asLong();
                if (!cameFrom.containsKey(key)) {
                    cameFrom.put(key, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!cameFrom.containsKey(to.asLong())) {
            return List.of();
        }

        LinkedList<BlockPos> path = new LinkedList<>();
        BlockPos step = to;
        while (step != null) {
            path.addFirst(step);
            step = cameFrom.get(step.asLong());
        }
        return path;
    }

    /** Same adjacency rule as the flood fill itself, but checked against the stored tile set instead of the live world. */
    private static List<BlockPos> geometricNeighbors(BlockPos from, Set<Long> tileSet) {
        List<BlockPos> found = new ArrayList<>(4);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : dirs) {
            for (int dy = 1; dy >= -1; dy--) {
                BlockPos candidate = from.offset(dir[0], dy, dir[1]);
                if (tileSet.contains(candidate.asLong())) {
                    found.add(candidate);
                    break;
                }
            }
        }
        return found;
    }
}
