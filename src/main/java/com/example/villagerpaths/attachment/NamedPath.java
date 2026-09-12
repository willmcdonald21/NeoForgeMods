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
 * chosen block palette) plus zero or more destination zones a villager can detour
 * into. Shared by every villager assigned to it.
 */
public record NamedPath(UUID id, String name, List<ResourceLocation> palette, List<BlockPos> networkTiles, List<Zone> zones) {
    public static final Codec<NamedPath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(NamedPath::id),
            Codec.STRING.fieldOf("name").forGetter(NamedPath::name),
            ResourceLocation.CODEC.listOf().fieldOf("palette").forGetter(NamedPath::palette),
            BlockPos.CODEC.listOf().fieldOf("networkTiles").forGetter(NamedPath::networkTiles),
            Zone.CODEC.listOf().fieldOf("zones").forGetter(NamedPath::zones)
    ).apply(instance, NamedPath::new));
}
