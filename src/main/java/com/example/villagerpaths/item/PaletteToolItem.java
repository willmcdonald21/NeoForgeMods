package com.example.villagerpaths.item;

import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import com.example.villagerpaths.attachment.PathStep;
import com.example.villagerpaths.attachment.Zone;

/**
 * Right-click blocks to toggle them into a walkable-surface palette for the
 * destination zone most recently placed in your active linking session; shift +
 * right-click that same villager to apply it. Restricts wandering to just that one
 * zone - other zones (on this or other paths) are unaffected.
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
        if (!hasEditableZone(session)) {
            player.displayClientMessage(Component.literal(
                    "Place a Destination Marker zone in your current path first, then use this to restrict its surfaces."), true);
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

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide() || usedHand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(interactionTarget instanceof Villager villager)) {
            return InteractionResult.PASS;
        }

        PathLinkingSession session = PathLinkingSessions.get(player.getUUID());
        if (session == null || !session.villagerId().equals(villager.getUUID()) || !hasEditableZone(session)) {
            player.displayClientMessage(Component.literal(
                    "Place a Destination Marker zone for this villager first, then apply the palette."), true);
            return InteractionResult.CONSUME;
        }

        Set<Block> palette = PaletteBuildSessions.getOrCreate(player.getUUID());
        List<ResourceLocation> surfaces = palette.stream().map(BuiltInRegistries.BLOCK::getKey).toList();

        int lastIndex = session.steps().size() - 1;
        PathStep lastStep = session.steps().get(lastIndex);
        Zone updatedZone = lastStep.lingerZone().orElseThrow().withAllowedSurfaces(surfaces);
        session.steps().set(lastIndex, PathStep.destination(lastStep.anchor(), updatedZone));

        PaletteBuildSessions.clear(player.getUUID());
        player.displayClientMessage(Component.literal(surfaces.isEmpty()
                ? "Zone left unrestricted (no blocks selected)."
                : "Palette applied: " + surfaces.size() + " block type(s)."), true);
        return InteractionResult.CONSUME;
    }

    private static boolean hasEditableZone(PathLinkingSession session) {
        return session != null && !session.steps().isEmpty()
                && session.steps().get(session.steps().size() - 1).lingerZone().isPresent();
    }
}
