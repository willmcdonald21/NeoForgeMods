package com.example.villagerpaths.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.VillagerPathData;

/**
 * Right-click a villager to start/finish linking a path to it; right-click ground
 * blocks in between to drop waypoints in order. Linking state is transient
 * (in-memory only, per player) and is not persisted across a server restart.
 */
public class PathMarkerItem extends Item {
    private static final Map<UUID, LinkingSession> ACTIVE_SESSIONS = new HashMap<>();

    public PathMarkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide() || usedHand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(interactionTarget instanceof Villager villager)) {
            return InteractionResult.PASS;
        }

        LinkingSession session = ACTIVE_SESSIONS.get(player.getUUID());

        if (session == null || !session.villagerId.equals(villager.getUUID())) {
            ACTIVE_SESSIONS.put(player.getUUID(), new LinkingSession(villager.getUUID()));
            player.displayClientMessage(Component.literal(
                    "Linking path: right-click ground to add waypoints, sneak + right-click this villager to save."), true);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            ACTIVE_SESSIONS.remove(player.getUUID());
            if (session.waypoints.isEmpty()) {
                player.displayClientMessage(Component.literal("No waypoints added; path not saved."), true);
            } else {
                villager.setData(ModAttachments.VILLAGER_PATH.get(), new VillagerPathData(List.copyOf(session.waypoints), 0));
                player.displayClientMessage(Component.literal(
                        "Path saved with " + session.waypoints.size() + " waypoint(s)."), true);
            }
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal("Right-click ground to add a waypoint, or sneak + right-click to save."), true);
        return InteractionResult.CONSUME;
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
        LinkingSession session = ACTIVE_SESSIONS.get(player.getUUID());
        if (session == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos().above();
        session.waypoints.add(pos);
        player.displayClientMessage(Component.literal("Waypoint " + session.waypoints.size() + " added."), true);
        return InteractionResult.CONSUME;
    }

    private static final class LinkingSession {
        private final UUID villagerId;
        private final List<BlockPos> waypoints = new ArrayList<>();

        private LinkingSession(UUID villagerId) {
            this.villagerId = villagerId;
        }
    }
}
