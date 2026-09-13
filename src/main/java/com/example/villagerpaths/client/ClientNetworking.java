package com.example.villagerpaths.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.example.villagerpaths.network.OpenDestinationManagerPayload;
import com.example.villagerpaths.network.OpenPathManagerPayload;
import com.example.villagerpaths.network.OpenVillagerPathScreenPayload;

/** Client-bound payload registration and handling. Only ever loaded on the client physical side. */
public final class ClientNetworking {
    private ClientNetworking() {
    }

    public static void registerClientbound(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(OpenVillagerPathScreenPayload.TYPE, OpenVillagerPathScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> Minecraft.getInstance().setScreen(new VillagerPathScreen(payload))));
        registrar.playToClient(OpenPathManagerPayload.TYPE, OpenPathManagerPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> Minecraft.getInstance().setScreen(new PathManagerScreen(payload))));
        registrar.playToClient(OpenDestinationManagerPayload.TYPE, OpenDestinationManagerPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> Minecraft.getInstance().setScreen(new DestinationManagerScreen(payload))));
    }
}
