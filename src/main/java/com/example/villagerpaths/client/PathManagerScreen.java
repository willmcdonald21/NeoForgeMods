package com.example.villagerpaths.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import com.example.villagerpaths.network.AddZoneToPathPayload;
import com.example.villagerpaths.network.DeletePathPayload;
import com.example.villagerpaths.network.OpenPathManagerPayload;
import com.example.villagerpaths.network.PathRef;
import com.example.villagerpaths.network.PathWithZones;
import com.example.villagerpaths.network.RemoveZoneFromPathPayload;

/**
 * Lists every named path with an Edit button each (add/remove its destinations),
 * plus one Delete button (pick a path from the dropdown, then delete it).
 */
public class PathManagerScreen extends Screen {
    private final OpenPathManagerPayload data;
    private PathWithZones editing;

    public PathManagerScreen(OpenPathManagerPayload data) {
        super(Component.literal("Path Manager"));
        this.data = data;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        if (editing != null) {
            initEditor();
        } else {
            initList();
        }
    }

    private void initList() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 80;

        if (data.paths().isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose()).bounds(centerX - 40, y, 80, 20).build());
            return;
        }

        for (PathWithZones path : data.paths()) {
            addRenderableWidget(Button.builder(Component.literal(path.name()), b -> {
            }).bounds(centerX - 100, y, 130, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Edit"), b -> {
                editing = path;
                this.init();
            }).bounds(centerX + 35, y, 65, 20).build());
            y += 24;
        }

        y += 10;
        List<PathRef> refs = data.paths().stream().map(p -> new PathRef(p.id(), p.name())).toList();
        CycleButton<PathRef> deleteCycle = CycleButton.<PathRef>builder(ref -> Component.literal(ref.name()))
                .withValues(refs)
                .create(centerX - 100, y, 130, 20, Component.literal("Delete which"), (button, value) -> {
                });
        addRenderableWidget(deleteCycle);
        addRenderableWidget(Button.builder(Component.literal("Delete"), b -> {
            PacketDistributor.sendToServer(new DeletePathPayload(deleteCycle.getValue().id()));
            this.onClose();
        }).bounds(centerX + 35, y, 65, 20).build());

        y += 30;
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose()).bounds(centerX - 40, y, 80, 20).build());
    }

    private void initEditor() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 80;

        for (PathRef zone : editing.zones()) {
            addRenderableWidget(Button.builder(Component.literal(zone.name()), b -> {
            }).bounds(centerX - 100, y, 130, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Remove"), b -> {
                PacketDistributor.sendToServer(new RemoveZoneFromPathPayload(editing.id(), zone.id()));
                List<PathRef> remaining = new ArrayList<>(editing.zones());
                remaining.remove(zone);
                editing = new PathWithZones(editing.id(), editing.name(), remaining);
                this.init();
            }).bounds(centerX + 35, y, 65, 20).build());
            y += 24;
        }

        y += 10;
        List<PathRef> available = data.allZones().stream()
                .filter(z -> editing.zones().stream().noneMatch(existing -> existing.id().equals(z.id())))
                .toList();
        if (!available.isEmpty()) {
            CycleButton<PathRef> addCycle = CycleButton.<PathRef>builder(ref -> Component.literal(ref.name()))
                    .withValues(available)
                    .create(centerX - 100, y, 130, 20, Component.literal("Add which"), (button, value) -> {
                    });
            addRenderableWidget(addCycle);
            addRenderableWidget(Button.builder(Component.literal("Add"), b -> {
                PathRef chosen = addCycle.getValue();
                PacketDistributor.sendToServer(new AddZoneToPathPayload(editing.id(), chosen.id()));
                List<PathRef> updated = new ArrayList<>(editing.zones());
                updated.add(chosen);
                editing = new PathWithZones(editing.id(), editing.name(), updated);
                this.init();
            }).bounds(centerX + 35, y, 65, 20).build());
            y += 24;
        }

        y += 10;
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            editing = null;
            this.init();
        }).bounds(centerX - 90, y, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose()).bounds(centerX + 10, y, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        String titleText = editing != null ? "Editing: " + editing.name() : "Path Manager";
        graphics.drawCenteredString(this.font, titleText, this.width / 2, this.height / 2 - 100, 0xFFFFFF);
        if (editing == null && data.paths().isEmpty()) {
            graphics.drawCenteredString(this.font, "No paths exist yet.", this.width / 2, this.height / 2 - 80, 0xAAAAAA);
        }
        if (editing != null && editing.zones().isEmpty()) {
            graphics.drawCenteredString(this.font, "No destinations on this path yet.", this.width / 2, this.height / 2 - 80, 0xAAAAAA);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
