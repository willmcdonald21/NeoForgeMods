package com.example.villagerpaths.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.NamedPath;
import com.example.villagerpaths.attachment.PathLibraries;
import com.example.villagerpaths.attachment.PathLibrary;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.attachment.ZoneLibraries;
import com.example.villagerpaths.attachment.ZoneLibrary;
import com.example.villagerpaths.item.DestinationMarkerItem;

/** Common (server-bound) payload registration and handling. */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerServerbound(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(AssignPathPayload.TYPE, AssignPathPayload.STREAM_CODEC, ModNetworking::handleAssign);
        registrar.playToServer(RenamePathPayload.TYPE, RenamePathPayload.STREAM_CODEC, ModNetworking::handleRename);
        registrar.playToServer(DeletePathPayload.TYPE, DeletePathPayload.STREAM_CODEC, ModNetworking::handleDeletePath);
        registrar.playToServer(DeleteZonePayload.TYPE, DeleteZonePayload.STREAM_CODEC, ModNetworking::handleDeleteZone);
        registrar.playToServer(RenameZonePayload.TYPE, RenameZonePayload.STREAM_CODEC, ModNetworking::handleRenameZone);
        registrar.playToServer(ArmZoneReshapePayload.TYPE, ArmZoneReshapePayload.STREAM_CODEC, ModNetworking::handleArmReshape);
        registrar.playToServer(AddZoneToPathPayload.TYPE, AddZoneToPathPayload.STREAM_CODEC, ModNetworking::handleAddZoneToPath);
        registrar.playToServer(RemoveZoneFromPathPayload.TYPE, RemoveZoneFromPathPayload.STREAM_CODEC, ModNetworking::handleRemoveZoneFromPath);
    }

    private static void handleAssign(AssignPathPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            Entity entity = serverPlayer.level().getEntity(payload.villagerEntityId());
            if (!(entity instanceof Villager villager)) {
                return;
            }
            if (payload.pathId().isPresent()) {
                PathLibrary library = PathLibraries.get(serverPlayer.getServer());
                if (library.find(payload.pathId().get()).isEmpty()) {
                    return;
                }
                villager.setData(ModAttachments.VILLAGER_PATH.get(), VillagerPathData.assigned(payload.pathId().get()));
            } else {
                villager.setData(ModAttachments.VILLAGER_PATH.get(), VillagerPathData.EMPTY);
            }
        });
    }

    private static void handleRename(RenamePathPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            String trimmed = payload.newName().trim();
            if (trimmed.isEmpty()) {
                return;
            }
            MinecraftServer server = serverPlayer.getServer();
            PathLibrary library = PathLibraries.get(server);
            library.find(payload.pathId()).ifPresent(named ->
                    PathLibraries.save(server, library.withPath(named.withName(trimmed))));
        });
    }

    private static void handleDeletePath(DeletePathPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            MinecraftServer server = serverPlayer.getServer();
            PathLibraries.save(server, PathLibraries.get(server).withoutPath(payload.pathId()));
        });
    }

    private static void handleDeleteZone(DeleteZonePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            MinecraftServer server = serverPlayer.getServer();

            ZoneLibrary zoneLibrary = ZoneLibraries.get(server);
            ZoneLibraries.save(server, zoneLibrary.withoutZone(payload.zoneId()));

            PathLibrary pathLibrary = PathLibraries.get(server);
            PathLibrary updated = pathLibrary;
            for (NamedPath path : pathLibrary.paths()) {
                if (path.zoneIds().contains(payload.zoneId())) {
                    List<UUID> newZoneIds = new ArrayList<>(path.zoneIds());
                    newZoneIds.remove(payload.zoneId());
                    updated = updated.withPath(path.withZoneIds(newZoneIds));
                }
            }
            PathLibraries.save(server, updated);
        });
    }

    private static void handleRenameZone(RenameZonePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            String trimmed = payload.newName().trim();
            if (trimmed.isEmpty()) {
                return;
            }
            MinecraftServer server = serverPlayer.getServer();
            ZoneLibrary library = ZoneLibraries.get(server);
            library.find(payload.zoneId()).ifPresent(named ->
                    ZoneLibraries.save(server, library.withZone(named.withName(trimmed))));
        });
    }

    private static void handleArmReshape(ArmZoneReshapePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            DestinationMarkerItem.armReshape(serverPlayer.getUUID(), payload.zoneId());
            serverPlayer.displayClientMessage(Component.literal(
                    "Right-click a corner with the Destination Marker to start reshaping."), true);
        });
    }

    private static void handleAddZoneToPath(AddZoneToPathPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            MinecraftServer server = serverPlayer.getServer();
            PathLibrary library = PathLibraries.get(server);
            library.find(payload.pathId()).ifPresent(path -> {
                if (!path.zoneIds().contains(payload.zoneId())) {
                    List<UUID> newZoneIds = new ArrayList<>(path.zoneIds());
                    newZoneIds.add(payload.zoneId());
                    PathLibraries.save(server, library.withPath(path.withZoneIds(newZoneIds)));
                }
            });
        });
    }

    private static void handleRemoveZoneFromPath(RemoveZoneFromPathPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            MinecraftServer server = serverPlayer.getServer();
            PathLibrary library = PathLibraries.get(server);
            library.find(payload.pathId()).ifPresent(path -> {
                List<UUID> newZoneIds = new ArrayList<>(path.zoneIds());
                newZoneIds.remove(payload.zoneId());
                PathLibraries.save(server, library.withPath(path.withZoneIds(newZoneIds)));
            });
        });
    }
}
