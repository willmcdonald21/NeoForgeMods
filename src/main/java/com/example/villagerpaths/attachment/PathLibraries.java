package com.example.villagerpaths.attachment;

import net.minecraft.server.MinecraftServer;

/** Access to the single, world-wide PathLibrary, always stored on the overworld regardless of dimension. */
public final class PathLibraries {
    private PathLibraries() {
    }

    public static PathLibrary get(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.PATH_LIBRARY.get());
    }

    public static void save(MinecraftServer server, PathLibrary library) {
        server.overworld().setData(ModAttachments.PATH_LIBRARY.get(), library);
    }
}
