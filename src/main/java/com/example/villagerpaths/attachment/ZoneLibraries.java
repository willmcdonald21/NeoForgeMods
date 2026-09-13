package com.example.villagerpaths.attachment;

import net.minecraft.server.MinecraftServer;

/** Access to the single, world-wide ZoneLibrary, always stored on the overworld regardless of dimension. */
public final class ZoneLibraries {
    private ZoneLibraries() {
    }

    public static ZoneLibrary get(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.ZONE_LIBRARY.get());
    }

    public static void save(MinecraftServer server, ZoneLibrary library) {
        server.overworld().setData(ModAttachments.ZONE_LIBRARY.get(), library);
    }
}
