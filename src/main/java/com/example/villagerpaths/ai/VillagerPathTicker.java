package com.example.villagerpaths.ai;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.example.villagerpaths.attachment.NamedZone;
import com.example.villagerpaths.attachment.NetworkFloodFill;
import com.example.villagerpaths.attachment.PathLibraries;
import com.example.villagerpaths.attachment.VillagerPathData;
import com.example.villagerpaths.attachment.Zone;
import com.example.villagerpaths.attachment.ZoneLibraries;
import com.example.villagerpaths.attachment.ZoneLibrary;

@EventBusSubscriber(modid = VillagerPathsMod.MODID)
public class VillagerPathTicker {
    private static final double ARRIVE_DISTANCE_SQ = 2.5 * 2.5;
    private static final double HOP_ARRIVE_DISTANCE_SQ = 1.2 * 1.2;

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
        if (namedPath.networkTiles().isEmpty() && namedPath.zoneIds().isEmpty()) {
            return;
        }

        ZoneLibrary zoneLibrary = ZoneLibraries.get(serverLevel.getServer());
        long gameTime = villager.level().getGameTime();

        if (data.currentGoal().isEmpty()) {
            pickNewGoal(villager, data, namedPath, zoneLibrary);
            return;
        }

        if (data.busyUntil() > gameTime) {
            if (data.zoneId().isPresent()) {
                zoneLibrary.find(data.zoneId().get())
                        .ifPresent(namedZone -> wanderInZone(villager, data, namedZone.shape(), gameTime));
            }
            // Otherwise it's a brief pause at a plain road tile - just stand still.
            return;
        }
        if (data.busyUntil() != 0L) {
            // Busy period just ended; pick the next goal.
            pickNewGoal(villager, data, namedPath, zoneLibrary);
            return;
        }

        // Walk the network hop by hop first, so travel never cuts across blocks outside the palette.
        if (!data.route().isEmpty()) {
            BlockPos hop = data.route().get(0);
            if (villager.blockPosition().distSqr(hop) <= HOP_ARRIVE_DISTANCE_SQ) {
                villager.setData(ModAttachments.VILLAGER_PATH.get(), data.advancedRoute());
                return;
            }
            if (!pursue(villager, hop)) {
                // Hop is temporarily unreachable (obstructed, etc.) - skip it rather than
                // stalling, which would otherwise leave vanilla AI free to wander elsewhere.
                villager.setData(ModAttachments.VILLAGER_PATH.get(), data.advancedRoute());
            }
            return;
        }

        // Route (if any) is exhausted; make the final approach to the goal itself.
        BlockPos goal = data.currentGoal().get();
        if (villager.blockPosition().distSqr(goal) <= ARRIVE_DISTANCE_SQ) {
            boolean isZoneGoal = data.zoneId().isPresent();
            long duration = isZoneGoal ? randomLingerTicks(villager.getRandom()) : randomWanderPauseTicks(villager.getRandom());
            villager.setData(ModAttachments.VILLAGER_PATH.get(), data.arrivedAt(gameTime + duration));
            return;
        }

        pursue(villager, goal);
    }

    /**
     * Cycles through the path's destination zones in a constant loop, in order, so the
     * villager's daytime routine is a predictable circuit rather than a random pick each
     * time. Only falls back to a random road tile when the path has no destinations at all.
     */
    private static void pickNewGoal(Villager villager, VillagerPathData data, NamedPath namedPath, ZoneLibrary zoneLibrary) {
        RandomSource random = villager.getRandom();

        List<NamedZone> zones = namedPath.zoneIds().stream().map(zoneLibrary::find)
                .filter(Optional::isPresent).map(Optional::get).toList();
        boolean hasZones = !zones.isEmpty();
        boolean hasTiles = !namedPath.networkTiles().isEmpty();
        if (!hasZones && !hasTiles) {
            return;
        }

        BlockPos target;
        Optional<UUID> zoneId;
        if (hasZones) {
            int previousIndex = data.zoneId().map(id -> {
                for (int i = 0; i < zones.size(); i++) {
                    if (zones.get(i).id().equals(id)) {
                        return i;
                    }
                }
                return -1;
            }).orElse(-1);
            NamedZone namedZone = zones.get((previousIndex + 1) % zones.size());
            zoneId = Optional.of(namedZone.id());
            target = namedZone.shape().randomPointInside(random);
        } else {
            zoneId = Optional.empty();
            target = namedPath.networkTiles().get(random.nextInt(namedPath.networkTiles().size()));
        }

        List<BlockPos> route = buildRoute(namedPath.networkTiles(), villager.blockPosition(), target);
        villager.setData(ModAttachments.VILLAGER_PATH.get(), data.travelingTo(target, zoneId, route));
    }

    /**
     * Finds the network tiles nearest the villager and the target, then routes between
     * them across the network graph. The very first/last leg (current position to the
     * network, or the network to a point inside a zone) is a short direct walk, since
     * that connecting gap isn't itself part of the mapped network.
     */
    private static List<BlockPos> buildRoute(List<BlockPos> networkTiles, BlockPos from, BlockPos to) {
        if (networkTiles.isEmpty()) {
            return List.of();
        }
        BlockPos nearestToStart = NetworkFloodFill.nearestTile(networkTiles, from);
        BlockPos nearestToTarget = NetworkFloodFill.nearestTile(networkTiles, to);
        if (nearestToStart == null || nearestToTarget == null) {
            return List.of();
        }
        return NetworkFloodFill.route(networkTiles, nearestToStart, nearestToTarget);
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

    /**
     * Ensures navigation is actively heading to the target, retrying if vanilla brain
     * behaviors derailed it or it stalled out short of arriving. Returns false if no path
     * to the target could be found at all, so callers can react (e.g. skip an obstructed hop)
     * instead of leaving the villager idle for other AI to take over.
     */
    private static boolean pursue(Villager villager, BlockPos target) {
        PathNavigation navigation = villager.getNavigation();
        if (target.equals(navigation.getTargetPos()) && !navigation.isDone()) {
            return true;
        }
        return navigation.moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, Config.PATH_FOLLOW_SPEED.get());
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
