package com.example.villagerpaths.ai;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.example.villagerpaths.Config;
import com.example.villagerpaths.VillagerPathsMod;
import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.VillagerHomeData;

/**
 * Once a villager has an assigned home bed, walks it to that exact bed and puts it
 * to sleep whenever vanilla's REST activity kicks in, instead of letting vanilla
 * claim whatever nearby bed it likes.
 */
@EventBusSubscriber(modid = VillagerPathsMod.MODID)
public class VillagerHomeTicker {
    private static final double BED_ARRIVE_DISTANCE_SQ = 1.5 * 1.5;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        VillagerHomeData home = villager.getData(ModAttachments.VILLAGER_HOME.get());
        Optional<BlockPos> bed = home.bed();
        if (bed.isEmpty()) {
            return;
        }
        BlockPos bedPos = bed.get();

        // Vanilla's own brain still tracks a separately-claimed home bed and will keep
        // trying to walk there unless its memory agrees with our assignment - otherwise
        // it fights our navigation even after we've already put the villager to sleep.
        GlobalPos globalBed = GlobalPos.of(villager.level().dimension(), bedPos);
        if (villager.getBrain().getMemory(MemoryModuleType.HOME).map(current -> !current.equals(globalBed)).orElse(true)) {
            villager.getBrain().setMemory(MemoryModuleType.HOME, globalBed);
        }

        boolean resting = villager.getBrain().isActive(Activity.REST);
        if (!resting) {
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return;
        }
        if (villager.isSleeping()) {
            // Belt-and-suspenders: stop any movement vanilla still tries to apply this tick.
            villager.getNavigation().stop();
            return;
        }

        if (villager.blockPosition().distSqr(bedPos) <= BED_ARRIVE_DISTANCE_SQ) {
            villager.startSleeping(bedPos);
            return;
        }

        PathNavigation navigation = villager.getNavigation();
        if (!bedPos.equals(navigation.getTargetPos())) {
            navigation.moveTo(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
        }
    }
}
