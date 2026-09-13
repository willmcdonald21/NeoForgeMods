package com.example.villagerpaths.attachment;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

/**
 * A named, reusable path: a road network (every tile flood-filled outward from a
 * chosen block palette) plus references to zero or more shared destination zones.
 */
public record NamedPath(UUID id, String name, List<ResourceLocation> palette, List<BlockPos> networkTiles, List<UUID> zoneIds) {
    public static final Codec<NamedPath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(NamedPath::id),
            Codec.STRING.fieldOf("name").forGetter(NamedPath::name),
            ResourceLocation.CODEC.listOf().fieldOf("palette").forGetter(NamedPath::palette),
            BlockPos.CODEC.listOf().fieldOf("networkTiles").forGetter(NamedPath::networkTiles),
            UUIDUtil.CODEC.listOf().optionalFieldOf("zoneIds", List.of()).forGetter(NamedPath::zoneIds)
    ).apply(instance, NamedPath::new));

    public NamedPath withName(String newName) {
        return new NamedPath(id, newName, palette, networkTiles, zoneIds);
    }

    public NamedPath withZoneIds(List<UUID> newZoneIds) {
        return new NamedPath(id, name, palette, networkTiles, List.copyOf(newZoneIds));
    }
}
