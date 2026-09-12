package com.example.villagerpaths.item;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/**
 * Right-click blocks to toggle them into the road-surface palette for the path
 * you're currently linking with the Path Marker. When you save that path, its
 * connected network of matching blocks is flood-filled from these and baked in.
 */
public class PaletteToolItem extends Item {
    public PaletteToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide() || context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        PathLinkingSession session = PathLinkingSessions.get(player.getUUID());
        if (session == null) {
            player.displayClientMessage(Component.literal("Link a villager with the Path Marker first."), true);
            return InteractionResult.CONSUME;
        }

        Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        Set<Block> palette = PaletteBuildSessions.getOrCreate(player.getUUID());
        String name = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (palette.remove(block)) {
            player.displayClientMessage(Component.literal("Removed " + name + " (" + palette.size() + " in palette)."), true);
        } else {
            palette.add(block);
            player.displayClientMessage(Component.literal("Added " + name + " (" + palette.size() + " in palette)."), true);
        }
        return InteractionResult.CONSUME;
    }
}
