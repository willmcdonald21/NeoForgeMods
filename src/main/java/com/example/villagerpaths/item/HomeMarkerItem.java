package com.example.villagerpaths.item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.attachment.VillagerHomeData;

/**
 * Right-click a bed to select it, then shift + right-click a villager to assign
 * that bed as its home. The villager will path there and sleep in it at night
 * instead of vanilla's automatic bed claiming. Independent of path linking.
 */
public class HomeMarkerItem extends Item {
    private static final Map<UUID, BlockPos> PENDING_BEDS = new HashMap<>();

    public HomeMarkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide() || context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        if (!state.is(BlockTags.BEDS)) {
            player.displayClientMessage(Component.literal("That's not a bed."), true);
            return InteractionResult.CONSUME;
        }

        PENDING_BEDS.put(player.getUUID(), pos);
        player.displayClientMessage(Component.literal("Bed selected. Shift + right-click a villager to assign it as their home."), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (player.level().isClientSide() || usedHand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(interactionTarget instanceof Villager villager)) {
            return InteractionResult.PASS;
        }

        BlockPos bed = PENDING_BEDS.remove(player.getUUID());
        if (bed == null) {
            player.displayClientMessage(Component.literal("Right-click a bed first to select it."), true);
            return InteractionResult.CONSUME;
        }

        villager.setData(ModAttachments.VILLAGER_HOME.get(), new VillagerHomeData(Optional.of(bed)));
        player.displayClientMessage(Component.literal("Assigned home bed to this villager."), true);
        return InteractionResult.CONSUME;
    }
}
