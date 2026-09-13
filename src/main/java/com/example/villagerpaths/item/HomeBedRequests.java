package com.example.villagerpaths.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks a player's pending "pick a bed for this villager" request, opened from the
 * Villager Path screen's Set Home Bed button. The next shift + right-click on a bed
 * fulfills it directly, independent of whichever item (or empty hand) the player holds.
 */
public final class HomeBedRequests {
    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    private HomeBedRequests() {
    }

    public static void request(UUID playerId, int villagerEntityId) {
        PENDING.put(playerId, villagerEntityId);
    }

    public static Integer take(UUID playerId) {
        return PENDING.remove(playerId);
    }
}
