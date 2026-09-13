package com.example.villagerpaths.event;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.example.villagerpaths.VillagerPathsMod;
import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.VillagerHomeData;
import com.example.villagerpaths.item.HomeBedRequests;

/**
 * Fulfills a pending "Set Home Bed" request (started from the Villager Path screen) the
 * next time the requesting player shift + right-clicks a bed. Only intercepts the click
 * when a request is actually pending, so normal bed use (sleeping, setting spawn) is
 * completely untouched otherwise.
 */
@EventBusSubscriber(modid = VillagerPathsMod.MODID)
public class HomeBedSelectionHandler {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Integer villagerEntityId = HomeBedRequests.take(serverPlayer.getUUID());
        if (villagerEntityId == null) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!state.is(BlockTags.BEDS)) {
            // Not a bed - put the request back and let this click behave normally.
            HomeBedRequests.request(serverPlayer.getUUID(), villagerEntityId);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.CONSUME);

        Entity entity = serverPlayer.level().getEntity(villagerEntityId);
        if (!(entity instanceof Villager villager)) {
            serverPlayer.displayClientMessage(Component.literal("That villager is no longer nearby."), true);
            return;
        }

        villager.setData(ModAttachments.VILLAGER_HOME.get(), new VillagerHomeData(Optional.of(event.getPos())));
        serverPlayer.displayClientMessage(Component.literal("Assigned home bed to this villager."), true);
    }
}
