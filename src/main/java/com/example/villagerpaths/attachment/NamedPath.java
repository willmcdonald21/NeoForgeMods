package com.example.villagerpaths.attachment;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

/** A named, reusable path: the step list is shared by every villager assigned to it. */
public record NamedPath(UUID id, String name, List<PathStep> steps) {
    public static final Codec<NamedPath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(NamedPath::id),
            Codec.STRING.fieldOf("name").forGetter(NamedPath::name),
            PathStep.CODEC.listOf().fieldOf("steps").forGetter(NamedPath::steps)
    ).apply(instance, NamedPath::new));
}
