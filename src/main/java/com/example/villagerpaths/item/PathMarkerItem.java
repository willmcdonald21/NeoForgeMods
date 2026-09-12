package com.example.villagerpaths.item;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.NamedPath;
import com.example.villagerpaths.attachment.NetworkFloodFill;
import com.example.villagerpaths.attachment.PathLibraries;
import com.example.villagerpaths.attachment.PathLibrary;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.attachment.Zone;

/**
 * Right-click a villager to start/finish linking a path to it. While linked, use
 * the Palette Tool to mark road-surface blocks and the Destination Marker to add
 * linger zones; sneak + right-click this villager again to flood-fill the
 * connected network from those blocks and save it all as a named path.
 */
public class PathMarkerItem extends Item {
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

        PathLinkingSession session = PathLinkingSessions.get(player.getUUID());

        if (session == null || !session.villagerId().equals(villager.getUUID())) {
            PathLinkingSessions.start(player.getUUID(), villager.getUUID());
            PaletteBuildSessions.clear(player.getUUID());
            player.displayClientMessage(Component.literal(
                    "Linking path: use the Palette Tool to mark road blocks, the Destination Marker to add zones, sneak + right-click this villager to save."), true);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            PathLinkingSessions.end(player.getUUID());
            Set<Block> palette = PaletteBuildSessions.getOrCreate(player.getUUID());
            PaletteBuildSessions.clear(player.getUUID());

            ServerLevel level = (ServerLevel) player.level();
            List<BlockPos> networkTiles = NetworkFloodFill.fill(level, villager.blockPosition(), palette);
            List<Zone> zones = List.copyOf(session.zones());

            if (networkTiles.isEmpty() && zones.isEmpty()) {
                player.displayClientMessage(Component.literal("No path blocks or zones found; path not saved."), true);
                return InteractionResult.CONSUME;
            }

            MinecraftServer server = level.getServer();
            PathLibrary library = PathLibraries.get(server);
            UUID pathId = UUID.randomUUID();
            String name = "Path " + (library.paths().size() + 1);
            List<ResourceLocation> paletteKeys = palette.stream().map(BuiltInRegistries.BLOCK::getKey).toList();
            PathLibraries.save(server, library.withPath(new NamedPath(pathId, name, paletteKeys, networkTiles, zones)));

            villager.setData(ModAttachments.VILLAGER_PATH.get(), VillagerPathData.assigned(pathId));
            player.displayClientMessage(Component.literal(
                    "Saved \"" + name + "\" with " + networkTiles.size() + " path tile(s) and " + zones.size() + " zone(s)."), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal(
                "Use the Palette Tool for road blocks or the Destination Marker for zones, or sneak + right-click to save."), true);
        return InteractionResult.CONSUME;
    }
}
