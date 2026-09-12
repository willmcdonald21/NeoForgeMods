package com.example.villagerpaths.network;

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

/** Common (server-bound) payload registration and handling. */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerServerbound(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(AssignPathPayload.TYPE, AssignPathPayload.STREAM_CODEC, ModNetworking::handleAssign);
        registrar.playToServer(RenamePathPayload.TYPE, RenamePathPayload.STREAM_CODEC, ModNetworking::handleRename);
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
            library.find(payload.pathId()).ifPresent(named -> {
                NamedPath renamed = new NamedPath(named.id(), trimmed, named.steps());
                PathLibraries.save(server, library.withPath(renamed));
            });
        });
    }
}
