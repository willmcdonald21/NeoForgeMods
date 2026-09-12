package com.example.villagerpaths.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.example.villagerpaths.Config;
import com.example.villagerpaths.VillagerPathsMod;
import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.PathStep;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.attachment.Zone;

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

        long gameTime = villager.level().getGameTime();
        PathStep step = data.currentStep();

        if (data.lingerUntil() > gameTime) {
            wanderInZone(villager, data, step);
            return;
        }
        if (data.lingerUntil() != 0L) {
            // Linger just expired; move on to the next step.
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.advanced());
            return;
        }

        BlockPos anchor = step.anchor();
        if (villager.blockPosition().distSqr(anchor) <= ARRIVE_DISTANCE_SQ) {
            if (step.lingerZone().isPresent()) {
                Zone zone = step.lingerZone().get();
                BlockPos firstTarget = zone.randomPointInside(villager.getRandom());
                long lingerDuration = randomLingerTicks(villager.getRandom());
                villager.setData(ModAttachments.VILLAGER_PATH.get(), data.lingeringUntil(gameTime + lingerDuration, firstTarget));
            } else {
                villager.setData(ModAttachments.VILLAGER_PATH.get(), data.advanced());
            }
            return;
        }

        moveTowards(villager, anchor);
    }

    private static void wanderInZone(Villager villager, VillagerPathData data, PathStep step) {
        Zone zone = step.lingerZone().orElseThrow();
        PathNavigation navigation = villager.getNavigation();
        BlockPos wanderTarget = data.wanderTarget().orElse(null);

        if (wanderTarget == null || navigation.isDone()) {
            BlockPos newTarget = zone.randomPointInside(villager.getRandom());
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.withWanderTarget(newTarget));
            navigation.moveTo(newTarget.getX() + 0.5, newTarget.getY(), newTarget.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
            return;
        }

        // Vanilla brain behaviors may still steal navigation control most ticks; re-assert ours.
        if (!wanderTarget.equals(navigation.getTargetPos())) {
            navigation.moveTo(wanderTarget.getX() + 0.5, wanderTarget.getY(), wanderTarget.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
        }
    }

    private static void moveTowards(Villager villager, BlockPos target) {
        PathNavigation navigation = villager.getNavigation();
        if (!target.equals(navigation.getTargetPos())) {
            navigation.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
        }
    }

    private static long randomLingerTicks(RandomSource random) {
        int min = Config.LINGER_MIN_SECONDS.get();
        int max = Math.max(min, Config.LINGER_MAX_SECONDS.get());
        int seconds = max > min ? min + random.nextInt(max - min + 1) : min;
        return seconds * 20L;
    }
}
