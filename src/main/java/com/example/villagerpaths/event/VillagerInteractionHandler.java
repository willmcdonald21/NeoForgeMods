package com.example.villagerpaths.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import com.example.villagerpaths.VillagerPathsMod;
import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.PathLibraries;
import com.example.villagerpaths.attachment.PathLibrary;
import com.example.villagerpaths.attachment.VillagerHomeData;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.network.OpenVillagerPathScreenPayload;
import com.example.villagerpaths.network.PathRef;

/** Shift + right-click a villager with an empty hand to open the path assignment/rename screen. */
@EventBusSubscriber(modid = VillagerPathsMod.MODID)
public class VillagerInteractionHandler {
    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown()) {
            return;
        }
        if (!event.getEntity().getMainHandItem().isEmpty()) {
            return;
        }
        if (!(event.getTarget() instanceof Villager villager)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        PathLibrary library = PathLibraries.get(serverPlayer.getServer());
        VillagerPathData data = villager.getData(ModAttachments.VILLAGER_PATH.get());

        Optional<PathRef> assigned = data.pathId()
                .flatMap(library::find)
                .map(named -> new PathRef(named.id(), named.name()));

        List<PathRef> allPaths = new ArrayList<>();
        for (var named : library.paths()) {
            allPaths.add(new PathRef(named.id(), named.name()));
        }

        VillagerHomeData home = villager.getData(ModAttachments.VILLAGER_HOME.get());
        PacketDistributor.sendToPlayer(serverPlayer, new OpenVillagerPathScreenPayload(villager.getId(), assigned, allPaths, home.bed().isPresent()));
    }
}
