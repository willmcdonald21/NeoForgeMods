package com.example.villagerpaths;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.example.villagerpaths.client.ClientNetworking;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = VillagerPathsMod.MODID, dist = Dist.CLIENT)
public class VillagerPathsModClient {
    public VillagerPathsModClient(ModContainer container, IEventBus modEventBus) {
        // Allows NeoForge to create a config screen for this mod's configs.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(ClientNetworking::registerClientbound);
    }
}
