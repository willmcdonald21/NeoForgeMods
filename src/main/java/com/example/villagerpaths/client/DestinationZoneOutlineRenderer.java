package com.example.villagerpaths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.example.villagerpaths.VillagerPathsMod;
import com.example.villagerpaths.item.DestinationMarkerItem;

/** Draws a live outline from the first placed zone corner to whatever block the player is currently looking at. */
@EventBusSubscriber(modid = VillagerPathsMod.MODID, value = Dist.CLIENT)
public class DestinationZoneOutlineRenderer {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        BlockPos corner = DestinationMarkerItem.getClientPendingCorner();
        if (corner == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockPos other = blockHit.getBlockPos().above();

        int minX = Math.min(corner.getX(), other.getX());
        int minY = Math.min(corner.getY(), other.getY());
        int minZ = Math.min(corner.getZ(), other.getZ());
        int maxX = Math.max(corner.getX(), other.getX()) + 1;
        int maxY = Math.max(corner.getY(), other.getY()) + 1;
        int maxZ = Math.max(corner.getZ(), other.getZ()) + 1;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, 1.0f, 0.85f, 0.2f, 1.0f);
        bufferSource.endBatch(RenderType.lines());

        poseStack.popPose();
    }
}
