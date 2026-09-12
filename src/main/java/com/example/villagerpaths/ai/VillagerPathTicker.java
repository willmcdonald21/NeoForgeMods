package com.example.villagerpaths.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.example.villagerpaths.Config;
import com.example.villagerpaths.VillagerPathsMod;
import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.VillagerPathData;

@EventBusSubscriber(modid = VillagerPathsMod.MODID)
public class VillagerPathTicker {
    private static final double ARRIVE_DISTANCE_SQ = 2.5 * 2.5;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        VillagerPathData data = villager.getData(ModAttachments.VILLAGER_PATH.get());
        // Defer to vanilla behavior once the villager is heading to/in bed for the night.
        if (!data.hasPath() || villager.getBrain().isActive(Activity.REST)) {
            return;
        }

        BlockPos target = data.currentWaypoint();
        if (villager.blockPosition().distSqr(target) <= ARRIVE_DISTANCE_SQ) {
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.advanced());
            return;
        }

        // Vanilla brain behaviors (wandering, socializing, etc.) also set a nav target most
        // ticks, so re-assert ours whenever something else has taken over navigation.
        PathNavigation navigation = villager.getNavigation();
        if (!target.equals(navigation.getTargetPos())) {
            navigation.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
        }
    }
}
