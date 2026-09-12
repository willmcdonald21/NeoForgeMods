package com.example.villagerpaths;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue PATH_FOLLOW_SPEED = BUILDER
            .comment("Movement speed modifier used while a villager is walking its assigned path.")
            .defineInRange("pathFollowSpeed", 0.5, 0.1, 2.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
