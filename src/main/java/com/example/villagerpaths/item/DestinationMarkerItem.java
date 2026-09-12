package com.example.villagerpaths.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import com.example.villagerpaths.attachment.PathStep;
import com.example.villagerpaths.attachment.Zone;

/**
 * Right-click a block to place the first zone corner (a live outline previews the
 * box out to wherever you're looking), then right-click again to lock in the
 * opposite corner. Only works while a Path Marker linking session is active for
 * this player, and appends the resulting zone as the next step of that path.
 */
public class DestinationMarkerItem extends Item {
    private static final Map<UUID, BlockPos> PENDING_CORNERS = new HashMap<>();
    private static volatile BlockPos clientPendingCorner;

    public DestinationMarkerItem(Properties properties) {
        super(properties);
    }

    public static BlockPos getClientPendingCorner() {
        return clientPendingCorner;
    }

    static void clearPendingCorner(UUID playerId) {
        PENDING_CORNERS.remove(playerId);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos clicked = context.getClickedPos().above();

        if (context.getLevel().isClientSide()) {
            clientPendingCorner = clientPendingCorner == null ? clicked : null;
            return InteractionResult.CONSUME;
        }

        PathLinkingSession session = PathLinkingSessions.get(player.getUUID());
        if (session == null) {
            player.displayClientMessage(Component.literal("Link a villager with the Path Marker first."), true);
            return InteractionResult.CONSUME;
        }

        BlockPos pending = PENDING_CORNERS.get(player.getUUID());
        if (pending == null) {
            PENDING_CORNERS.put(player.getUUID(), clicked);
            player.displayClientMessage(Component.literal("Zone corner set. Right-click the opposite corner to finish."), true);
        } else {
            PENDING_CORNERS.remove(player.getUUID());
            Zone zone = new Zone(pending, clicked);
            session.steps().add(PathStep.destination(pending, zone));
            player.displayClientMessage(Component.literal("Destination zone added as step " + session.steps().size() + "."), true);
        }
        return InteractionResult.CONSUME;
    }
}
