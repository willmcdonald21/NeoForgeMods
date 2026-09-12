package com.example.villagerpaths.ai;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import com.example.villagerpaths.attachment.NamedPath;
import com.example.villagerpaths.attachment.PathLibraries;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.attachment.Zone;

@EventBusSubscriber(modid = VillagerPathsMod.MODID)
public class VillagerPathTicker {
    private static final double ARRIVE_DISTANCE_SQ = 2.5 * 2.5;
    private static final int ZONE_PICK_CHANCE_PERCENT = 90;

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
        if (data.pathId().isEmpty() || villager.getBrain().isActive(Activity.REST)) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) villager.level();
        Optional<NamedPath> namedPathOpt = PathLibraries.get(serverLevel.getServer()).find(data.pathId().get());
        if (namedPathOpt.isEmpty()) {
            return;
        }
        NamedPath namedPath = namedPathOpt.get();
        if (namedPath.networkTiles().isEmpty() && namedPath.zones().isEmpty()) {
            return;
        }

        long gameTime = villager.level().getGameTime();

        if (data.currentGoal().isEmpty()) {
            pickNewGoal(villager, data, namedPath);
            return;
        }

        if (data.busyUntil() > gameTime) {
            if (data.zoneIndex() >= 0 && data.zoneIndex() < namedPath.zones().size()) {
                wanderInZone(villager, data, namedPath.zones().get(data.zoneIndex()), gameTime);
            }
            // Otherwise it's a brief pause at a plain road tile - just stand still.
            return;
        }
        if (data.busyUntil() != 0L) {
            // Busy period just ended; pick the next goal.
            pickNewGoal(villager, data, namedPath);
            return;
        }

        BlockPos goal = data.currentGoal().get();
        if (villager.blockPosition().distSqr(goal) <= ARRIVE_DISTANCE_SQ) {
            boolean isZoneGoal = data.zoneIndex() >= 0;
            long duration = isZoneGoal ? randomLingerTicks(villager.getRandom()) : randomWanderPauseTicks(villager.getRandom());
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.arrivedAt(gameTime + duration));
            return;
        }

        moveTowards(villager, goal);
    }

    /** 90% of the time head to a random destination zone; otherwise wander to a random road tile. */
    private static void pickNewGoal(Villager villager, VillagerPathData data, NamedPath namedPath) {
        RandomSource random = villager.getRandom();
        boolean hasZones = !namedPath.zones().isEmpty();
        boolean hasTiles = !namedPath.networkTiles().isEmpty();

        boolean pickZone = hasZones && (!hasTiles || random.nextInt(100) < ZONE_PICK_CHANCE_PERCENT);

        if (pickZone) {
            int index = random.nextInt(namedPath.zones().size());
            Zone zone = namedPath.zones().get(index);
            BlockPos target = zone.randomPointInside(random);
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.travelingTo(target, index));
        } else if (hasTiles) {
            BlockPos target = namedPath.networkTiles().get(random.nextInt(namedPath.networkTiles().size()));
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.travelingTo(target, -1));
        }
    }

    /**
     * While lingering in a zone, alternates between walking to a random point in it
     * and standing still there for a short random pause before picking the next point.
     */
    private static void wanderInZone(Villager villager, VillagerPathData data, Zone zone, long gameTime) {
        if (data.wanderPauseUntil() > gameTime) {
            return; // standing still, pause not yet over
        }

        PathNavigation navigation = villager.getNavigation();
        BlockPos wanderTarget = data.wanderTarget().orElse(null);

        // No target yet, or a pause just ended: pick somewhere new to walk to.
        if (wanderTarget == null || data.wanderPauseUntil() != 0L) {
            BlockPos newTarget = zone.randomPointInside(villager.getRandom());
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.wanderingTowards(newTarget));
            navigation.moveTo(newTarget.getX() + 0.5, newTarget.getY(), newTarget.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
            return;
        }

        if (navigation.isDone()) {
            // Arrived: stand here for a short random pause before moving again.
            long pauseDuration = randomWanderPauseTicks(villager.getRandom());
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.pausingUntil(gameTime + pauseDuration));
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

    private static long randomWanderPauseTicks(RandomSource random) {
        int min = Config.WANDER_PAUSE_MIN_SECONDS.get();
        int max = Math.max(min, Config.WANDER_PAUSE_MAX_SECONDS.get());
        int seconds = max > min ? min + random.nextInt(max - min + 1) : min;
        return seconds * 20L;
    }
}
