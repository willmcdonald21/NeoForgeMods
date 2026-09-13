package com.example.villagerpaths.item;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.NamedPath;
import com.example.villagerpaths.attachment.NetworkFloodFill;
import com.example.villagerpaths.attachment.PathLibraries;
import com.example.villagerpaths.attachment.PathLibrary;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.attachment.ZoneLibraries;
import com.example.villagerpaths.attachment.ZoneLibrary;
import com.example.villagerpaths.network.OpenPathManagerPayload;
import com.example.villagerpaths.network.PathRef;
import com.example.villagerpaths.network.PathWithZones;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Right-click a villager to start/finish linking a path to it. While linked, use
 * the Palette Tool to mark road-surface blocks and the Destination Marker to add
 * linger zones; sneak + right-click this villager again to flood-fill the
 * connected network from those blocks and save it all as a named path. Shift +
 * right-click with nothing in reach opens the Path Manager screen.
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
            List<UUID> zoneIds = List.copyOf(session.zoneIds());

            if (networkTiles.isEmpty() && zoneIds.isEmpty()) {
                player.displayClientMessage(Component.literal("No path blocks or zones found; path not saved."), true);
                return InteractionResult.CONSUME;
            }

            MinecraftServer server = level.getServer();
            PathLibrary library = PathLibraries.get(server);
            UUID pathId = UUID.randomUUID();
            String name = "Path " + (library.paths().size() + 1);
            List<ResourceLocation> paletteKeys = palette.stream().map(BuiltInRegistries.BLOCK::getKey).toList();
            PathLibraries.save(server, library.withPath(new NamedPath(pathId, name, paletteKeys, networkTiles, zoneIds)));

            villager.setData(ModAttachments.VILLAGER_PATH.get(), VillagerPathData.assigned(pathId));
            player.displayClientMessage(Component.literal(
                    "Saved \"" + name + "\" with " + networkTiles.size() + " path tile(s) and " + zoneIds.size() + " zone(s)."), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.literal(
                "Use the Palette Tool for road blocks or the Destination Marker for zones, or sneak + right-click to save."), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        MinecraftServer server = serverPlayer.getServer();
        PathLibrary pathLibrary = PathLibraries.get(server);
        ZoneLibrary zoneLibrary = ZoneLibraries.get(server);

        List<PathWithZones> paths = pathLibrary.paths().stream()
                .map(named -> new PathWithZones(named.id(), named.name(), zoneRefs(named.zoneIds(), zoneLibrary)))
                .toList();
        List<PathRef> allZones = zoneLibrary.zones().stream().map(z -> new PathRef(z.id(), z.name())).toList();

        PacketDistributor.sendToPlayer(serverPlayer, new OpenPathManagerPayload(paths, allZones));
        return InteractionResultHolder.success(stack);
    }

    private static List<PathRef> zoneRefs(List<UUID> zoneIds, ZoneLibrary zoneLibrary) {
        return zoneIds.stream()
                .map(zoneLibrary::find)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(z -> new PathRef(z.id(), z.name()))
                .toList();
    }
}
