package com.example.villagerpaths;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue PATH_FOLLOW_SPEED = BUILDER
            .comment("Movement speed modifier used while a villager is walking its assigned path.")
            .defineInRange("pathFollowSpeed", 0.5, 0.1, 2.0);

    public static final ModConfigSpec.IntValue LINGER_MIN_SECONDS = BUILDER
            .comment("Minimum time (in seconds) a villager lingers in a destination zone before continuing its path.")
            .defineInRange("lingerMinSeconds", 15, 0, 3600);

    public static final ModConfigSpec.IntValue LINGER_MAX_SECONDS = BUILDER
            .comment("Maximum time (in seconds) a villager lingers in a destination zone before continuing its path.")
            .defineInRange("lingerMaxSeconds", 60, 0, 3600);

    static final ModConfigSpec SPEC = BUILDER.build();
}
