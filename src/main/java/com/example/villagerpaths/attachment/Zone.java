package com.example.villagerpaths.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

/** An axis-aligned rectangular zone (defined by two opposite corners) that a villager wanders within while lingering. */
public record Zone(BlockPos corner1, BlockPos corner2) {
    public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("corner1").forGetter(Zone::corner1),
            BlockPos.CODEC.fieldOf("corner2").forGetter(Zone::corner2)
    ).apply(instance, Zone::new));

    public BlockPos randomPointInside(RandomSource random) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        int x = minX + random.nextInt(maxX - minX + 1);
        int z = minZ + random.nextInt(maxZ - minZ + 1);
        return new BlockPos(x, corner1.getY(), z);
    }
}
