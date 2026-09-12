package com.example.villagerpaths.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.example.villagerpaths.VillagerPathsMod;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VillagerPathsMod.MODID);

    public static final DeferredItem<PathMarkerItem> PATH_MARKER = ITEMS.registerItem(
            "path_marker",
            PathMarkerItem::new,
            new Item.Properties().stacksTo(1));

    public static final DeferredItem<DestinationMarkerItem> DESTINATION_MARKER = ITEMS.registerItem(
            "destination_marker",
            DestinationMarkerItem::new,
            new Item.Properties().stacksTo(1));

    public static final DeferredItem<HomeMarkerItem> HOME_MARKER = ITEMS.registerItem(
            "home_marker",
            HomeMarkerItem::new,
            new Item.Properties().stacksTo(1));

    public static final DeferredItem<PaletteToolItem> PALETTE_TOOL = ITEMS.registerItem(
            "palette_tool",
            PaletteToolItem::new,
            new Item.Properties().stacksTo(1));
}
