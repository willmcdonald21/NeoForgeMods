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
}
