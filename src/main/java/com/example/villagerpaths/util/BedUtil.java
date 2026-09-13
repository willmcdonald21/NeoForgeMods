package com.example.villagerpaths.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/** Bed-related helpers. */
public final class BedUtil {
    private BedUtil() {
    }

    /**
     * Normalizes a bed position to its head half. Vanilla's own bed interaction
     * (BedBlock#useWithoutItem) does this same normalization before calling
     * startSleeping - the sleeping-pose render offset is computed assuming the anchor
     * is the head half, so sleeping at the foot half instead leaves half the body
     * hanging off the bed in the air.
     */
    public static BlockPos normalizeToHeadPart(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock) || state.getValue(BedBlock.PART) == BedPart.HEAD) {
            return pos;
        }
        return pos.relative(BedBlock.getConnectedDirection(state));
    }
}
