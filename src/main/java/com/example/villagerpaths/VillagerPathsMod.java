package com.example.villagerpaths;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.example.villagerpaths.attachment.ModAttachments;
import com.example.villagerpaths.item.ModItems;
import com.example.villagerpaths.network.ModNetworking;

@Mod(VillagerPathsMod.MODID)
public class VillagerPathsMod {
    public static final String MODID = "villagerpaths";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VILLAGER_PATHS_TAB =
            CREATIVE_MODE_TABS.register("villager_paths_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.villagerpaths"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.PATH_MARKER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PATH_MARKER.get());
                        output.accept(ModItems.DESTINATION_MARKER.get());
                        output.accept(ModItems.HOME_MARKER.get());
                        output.accept(ModItems.PALETTE_TOOL.get());
                    })
                    .build());

    public VillagerPathsMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(ModNetworking::registerServerbound);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
