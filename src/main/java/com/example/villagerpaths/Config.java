package com.example.villagerpaths;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Minecraft day-time ticks: 0 = dawn, 6000 = noon, 12000 = dusk, 18000 = midnight.
    public static final ModConfigSpec.IntValue SCHEDULE_START_TIME = BUILDER
            .comment("Day-time tick (0-24000) when villagers start following their assigned path.")
            .defineInRange("scheduleStartTime", 0, 0, 24000);

    public static final ModConfigSpec.IntValue SCHEDULE_END_TIME = BUILDER
            .comment("Day-time tick (0-24000) when villagers stop following their assigned path and resume vanilla behavior.")
            .defineInRange("scheduleEndTime", 12000, 0, 24000);

    public static final ModConfigSpec.DoubleValue PATH_FOLLOW_SPEED = BUILDER
            .comment("Movement speed modifier used while a villager is walking its assigned path.")
            .defineInRange("pathFollowSpeed", 0.5, 0.1, 2.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
