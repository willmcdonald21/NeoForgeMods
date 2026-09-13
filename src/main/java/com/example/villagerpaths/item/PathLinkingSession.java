package com.example.villagerpaths.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** In-progress network path being built for one villager by one player. Transient, in-memory only. */
public class PathLinkingSession {
    private final UUID villagerId;
    private final List<UUID> zoneIds = new ArrayList<>();

    PathLinkingSession(UUID villagerId) {
        this.villagerId = villagerId;
    }

    public UUID villagerId() {
        return villagerId;
    }

    public List<UUID> zoneIds() {
        return zoneIds;
    }
}
