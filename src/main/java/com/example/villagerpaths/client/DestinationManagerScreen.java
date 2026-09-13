package com.example.villagerpaths.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import com.example.villagerpaths.network.ArmZoneReshapePayload;
import com.example.villagerpaths.network.DeleteZonePayload;
import com.example.villagerpaths.network.OpenDestinationManagerPayload;
import com.example.villagerpaths.network.PathRef;
import com.example.villagerpaths.network.RenameZonePayload;

/**
 * Lists every named destination with a rename (pencil) and Edit (reshape) button
 * each, plus one Delete button (pick a destination from the dropdown, then delete it).
 */
public class DestinationManagerScreen extends Screen {
    private final List<PathRef> zones;
    private UUID renamingId;
    private EditBox nameBox;

    public DestinationManagerScreen(OpenDestinationManagerPayload data) {
        super(Component.literal("Destination Manager"));
        this.zones = new ArrayList<>(data.allZones());
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int centerX = this.width / 2;
        int y = this.height / 2 - 80;

        if (zones.isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose()).bounds(centerX - 40, y, 80, 20).build());
            return;
        }

        for (PathRef zone : zones) {
            if (zone.id().equals(renamingId)) {
                nameBox = new EditBox(this.font, centerX - 100, y, 110, 20, Component.literal("Destination name"));
                nameBox.setMaxLength(32);
                nameBox.setValue(zone.name());
                addRenderableWidget(nameBox);
                addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
                    PacketDistributor.sendToServer(new RenameZonePayload(zone.id(), nameBox.getValue()));
                    this.onClose();
                }).bounds(centerX + 15, y, 85, 20).build());
            } else {
                addRenderableWidget(Button.builder(Component.literal(zone.name()), b -> {
                }).bounds(centerX - 100, y, 90, 20).build());
                addRenderableWidget(Button.builder(Component.literal("✎"), b -> {
                    renamingId = zone.id();
                    this.init();
                }).bounds(centerX - 5, y, 20, 20).build());
                addRenderableWidget(Button.builder(Component.literal("Edit"), b -> {
                    PacketDistributor.sendToServer(new ArmZoneReshapePayload(zone.id()));
                    this.onClose();
                }).bounds(centerX + 20, y, 80, 20).build());
            }
            y += 24;
        }

        y += 10;
        CycleButton<PathRef> deleteCycle = CycleButton.<PathRef>builder(ref -> Component.literal(ref.name()))
                .withValues(zones)
                .create(centerX - 100, y, 130, 20, Component.literal("Delete which"), (button, value) -> {
                });
        addRenderableWidget(deleteCycle);
        addRenderableWidget(Button.builder(Component.literal("Delete"), b -> {
            PacketDistributor.sendToServer(new DeleteZonePayload(deleteCycle.getValue().id()));
            this.onClose();
        }).bounds(centerX + 35, y, 65, 20).build());

        y += 30;
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose()).bounds(centerX - 40, y, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 100, 0xFFFFFF);
        if (zones.isEmpty()) {
            graphics.drawCenteredString(this.font, "No destinations exist yet.", this.width / 2, this.height / 2 - 80, 0xAAAAAA);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
