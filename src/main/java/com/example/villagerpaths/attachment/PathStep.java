package com.example.villagerpaths.attachment;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

/**
 * One step in a villager's path: a position to walk to, and optionally a zone to
 * linger and wander around in (for a random duration) once that position is reached.
 */
public record PathStep(BlockPos anchor, Optional<Zone> lingerZone) {
    public static final Codec<PathStep> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("anchor").forGetter(PathStep::anchor),
            Zone.CODEC.optionalFieldOf("zone").forGetter(PathStep::lingerZone)
    ).apply(instance, PathStep::new));

    public static PathStep waypoint(BlockPos pos) {
        return new PathStep(pos, Optional.empty());
    }

    public static PathStep destination(BlockPos anchor, Zone zone) {
        return new PathStep(anchor, Optional.of(zone));
    }
}
