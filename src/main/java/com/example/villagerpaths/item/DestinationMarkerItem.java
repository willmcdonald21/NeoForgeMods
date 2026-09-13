package com.example.villagerpaths.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.example.villagerpaths.attachment.NamedZone;
import com.example.villagerpaths.attachment.Zone;
import com.example.villagerpaths.attachment.ZoneLibraries;
import com.example.villagerpaths.attachment.ZoneLibrary;
import com.example.villagerpaths.network.OpenDestinationManagerPayload;
import com.example.villagerpaths.network.PathRef;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Right-click a block to place the first zone corner (a live outline previews the
 * box out to wherever you're looking), then right-click again to lock in the
 * opposite corner. Normally requires an active Path Marker linking session and
 * adds a brand new destination to that path; if a destination is currently armed
 * for reshaping (via the Destination Manager screen), the same two clicks instead
 * update that destination's shape in place. Shift + right-click with nothing in
 * reach opens the Destination Manager screen.
 */
public class DestinationMarkerItem extends Item {
    private static final Map<UUID, BlockPos> PENDING_CORNERS = new HashMap<>();
    private static final Map<UUID, UUID> RESHAPE_TARGETS = new HashMap<>();
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

    public static void armReshape(UUID playerId, UUID zoneId) {
        RESHAPE_TARGETS.put(playerId, zoneId);
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

        UUID reshapeZoneId = RESHAPE_TARGETS.get(player.getUUID());
        BlockPos pending = PENDING_CORNERS.get(player.getUUID());

        if (reshapeZoneId != null) {
            if (pending == null) {
                PENDING_CORNERS.put(player.getUUID(), clicked);
                player.displayClientMessage(Component.literal("New corner set. Right-click the opposite corner to finish reshaping."), true);
            } else {
                PENDING_CORNERS.remove(player.getUUID());
                RESHAPE_TARGETS.remove(player.getUUID());
                Zone newShape = Zone.fromCorners(context.getLevel(), pending, clicked);
                MinecraftServer server = ((ServerLevel) context.getLevel()).getServer();
                ZoneLibrary library = ZoneLibraries.get(server);
                library.find(reshapeZoneId).ifPresentOrElse(namedZone -> {
                    ZoneLibraries.save(server, library.withZone(namedZone.withShape(newShape)));
                    player.displayClientMessage(Component.literal("Destination \"" + namedZone.name() + "\" reshaped."), true);
                }, () -> player.displayClientMessage(Component.literal("That destination no longer exists."), true));
            }
            return InteractionResult.CONSUME;
        }

        PathLinkingSession session = PathLinkingSessions.get(player.getUUID());
        if (session == null) {
            player.displayClientMessage(Component.literal("Link a villager with the Path Marker first."), true);
            return InteractionResult.CONSUME;
        }

        if (pending == null) {
            PENDING_CORNERS.put(player.getUUID(), clicked);
            player.displayClientMessage(Component.literal("Zone corner set. Right-click the opposite corner to finish."), true);
        } else {
            PENDING_CORNERS.remove(player.getUUID());
            Zone shape = Zone.fromCorners(context.getLevel(), pending, clicked);

            MinecraftServer server = ((ServerLevel) context.getLevel()).getServer();
            ZoneLibrary library = ZoneLibraries.get(server);
            UUID zoneId = UUID.randomUUID();
            String name = "Destination " + (library.zones().size() + 1);
            ZoneLibraries.save(server, library.withZone(new NamedZone(zoneId, name, shape)));

            session.zoneIds().add(zoneId);
            player.displayClientMessage(Component.literal(
                    "Destination \"" + name + "\" added (" + session.zoneIds().size() + " total)."), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ZoneLibrary library = ZoneLibraries.get(serverPlayer.getServer());
        List<PathRef> allZones = library.zones().stream().map(z -> new PathRef(z.id(), z.name())).toList();

        PacketDistributor.sendToPlayer(serverPlayer, new OpenDestinationManagerPayload(allZones));
        return InteractionResultHolder.success(stack);
    }
}
