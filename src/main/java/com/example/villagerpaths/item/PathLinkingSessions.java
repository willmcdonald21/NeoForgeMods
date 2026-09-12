package com.example.villagerpaths.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Shared registry of active per-player path-linking sessions, used by both the Path Marker and Destination Marker items. */
public final class PathLinkingSessions {
    private static final Map<UUID, PathLinkingSession> ACTIVE = new HashMap<>();

    private PathLinkingSessions() {
    }

    public static PathLinkingSession start(UUID playerId, UUID villagerId) {
        PathLinkingSession session = new PathLinkingSession(villagerId);
        ACTIVE.put(playerId, session);
        DestinationMarkerItem.clearPendingCorner(playerId);
        return session;
    }

    public static PathLinkingSession get(UUID playerId) {
        return ACTIVE.get(playerId);
    }

    public static PathLinkingSession end(UUID playerId) {
        DestinationMarkerItem.clearPendingCorner(playerId);
        return ACTIVE.remove(playerId);
    }
}
