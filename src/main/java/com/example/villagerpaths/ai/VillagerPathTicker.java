package com.example.villagerpaths.ai;

import net.minecraft.core.BlockPos;
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
        if (!data.hasPath() || !isInScheduleWindow(villager.level().getDayTime())) {
            return;
        }

        BlockPos target = data.currentWaypoint();
        if (villager.getNavigation().isDone()) {
            villager.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
        }
        if (villager.blockPosition().distSqr(target) <= ARRIVE_DISTANCE_SQ) {
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.advanced());
        }
    }

    private static boolean isInScheduleWindow(long dayTime) {
        long time = dayTime % 24000;
        long start = Config.SCHEDULE_START_TIME.get();
        long end = Config.SCHEDULE_END_TIME.get();
        if (start <= end) {
            return time >= start && time < end;
        }
        return time >= start || time < end;
    }
}
