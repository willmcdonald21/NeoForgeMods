package com.example.villagerpaths.attachment;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

/** A named, reusable destination zone. Shared by every path that uses it. */
public record NamedZone(UUID id, String name, Zone shape) {
    public static final Codec<NamedZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(NamedZone::id),
            Codec.STRING.fieldOf("name").forGetter(NamedZone::name),
            Zone.CODEC.fieldOf("shape").forGetter(NamedZone::shape)
    ).apply(instance, NamedZone::new));

    public NamedZone withShape(Zone newShape) {
        return new NamedZone(id, name, newShape);
    }

    public NamedZone withName(String newName) {
        return new NamedZone(id, newName, shape);
    }
}
