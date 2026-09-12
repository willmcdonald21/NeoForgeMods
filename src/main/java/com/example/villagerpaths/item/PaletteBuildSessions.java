package com.example.villagerpaths.item;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.level.block.Block;

/** Transient, per-player in-progress surface palette being built with the Palette Tool. */
final class PaletteBuildSessions {
    private static final Map<UUID, Set<Block>> SESSIONS = new ConcurrentHashMap<>();

    private PaletteBuildSessions() {
    }

    static Set<Block> getOrCreate(UUID playerId) {
        return SESSIONS.computeIfAbsent(playerId, id -> new LinkedHashSet<>());
    }

    static void clear(UUID playerId) {
        SESSIONS.remove(playerId);
    }
}
