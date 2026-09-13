package com.example.villagerpaths.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import com.example.villagerpaths.network.AssignPathPayload;
import com.example.villagerpaths.network.ClearHomeBedPayload;
import com.example.villagerpaths.network.OpenVillagerPathScreenPayload;
import com.example.villagerpaths.network.PathRef;
import com.example.villagerpaths.network.RenamePathPayload;
import com.example.villagerpaths.network.RequestHomeBedPayload;

/** Simple vanilla-style screen: rename the assigned path, or pick/switch a path from the shared library. */
public class VillagerPathScreen extends Screen {
    private static final UUID NONE_ID = new UUID(0, 0);
    private static final PathRef NONE = new PathRef(NONE_ID, "None");

    private final OpenVillagerPathScreenPayload data;
    private boolean renaming;
    private EditBox nameBox;
    private CycleButton<PathRef> pathCycle;
    private int labelY;
    private boolean hasHomeBed;
    private int homeLabelY;

    public VillagerPathScreen(OpenVillagerPathScreenPayload data) {
        super(Component.literal("Villager Path"));
        this.data = data;
        this.hasHomeBed = data.hasHomeBed();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int centerX = this.width / 2;
        int y = this.height / 2 - 60;
        labelY = y;

        if (renaming) {
            nameBox = new EditBox(this.font, centerX - 100, y, 150, 20, Component.literal("Path name"));
            nameBox.setMaxLength(32);
            nameBox.setValue(data.assigned().map(PathRef::name).orElse(""));
            addRenderableWidget(nameBox);
            addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
                data.assigned().ifPresent(ref ->
                        PacketDistributor.sendToServer(new RenamePathPayload(ref.id(), nameBox.getValue())));
                this.onClose();
            }).bounds(centerX + 55, y, 70, 20).build());
        } else if (data.assigned().isPresent()) {
            addRenderableWidget(Button.builder(Component.literal("✎ Rename"), b -> {
                renaming = true;
                this.init();
            }).bounds(centerX - 60, y, 120, 20).build());
        }

        y += 30;
        if (!renaming && !data.allPaths().isEmpty()) {
            List<PathRef> options = new ArrayList<>();
            options.add(NONE);
            options.addAll(data.allPaths());
            PathRef initial = data.assigned().orElse(NONE);

            pathCycle = CycleButton.<PathRef>builder(ref -> Component.literal(ref.name()))
                    .withValues(options)
                    .withInitialValue(initial)
                    .create(centerX - 100, y, 150, 20, Component.literal("Assign Path"), (button, value) -> {
                    });
            addRenderableWidget(pathCycle);

            y += 25;
            addRenderableWidget(Button.builder(Component.literal("Assign"), b -> {
                PathRef selected = pathCycle.getValue();
                Optional<UUID> chosen = selected.id().equals(NONE_ID) ? Optional.empty() : Optional.of(selected.id());
                PacketDistributor.sendToServer(new AssignPathPayload(data.villagerEntityId(), chosen));
                this.onClose();
            }).bounds(centerX - 40, y, 80, 20).build());
        }

        y += 35;
        if (!renaming) {
            homeLabelY = y;
            addRenderableWidget(Button.builder(Component.literal("Set Home Bed"), b -> {
                PacketDistributor.sendToServer(new RequestHomeBedPayload(data.villagerEntityId()));
                this.onClose();
            }).bounds(centerX - 100, y + 12, 95, 20).build());
            Button clearHome = Button.builder(Component.literal("Clear Home"), b -> {
                PacketDistributor.sendToServer(new ClearHomeBedPayload(data.villagerEntityId()));
                hasHomeBed = false;
                this.init();
            }).bounds(centerX + 5, y + 12, 95, 20).build();
            clearHome.active = hasHomeBed;
            addRenderableWidget(clearHome);
            y += 37;
        }

        y += 10;
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose()).bounds(centerX - 40, y, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFF);
        if (!renaming) {
            String label = data.assigned().map(ref -> "Path: " + ref.name()).orElse("No path assigned");
            graphics.drawCenteredString(this.font, label, this.width / 2, labelY - 12, 0xFFFFFF);
            if (data.allPaths().isEmpty()) {
                graphics.drawCenteredString(this.font, "No paths exist yet - create one with the Path Marker.", this.width / 2, labelY + 35, 0xAAAAAA);
            }
            String homeLabel = hasHomeBed ? "Home bed: assigned" : "Home bed: none";
            graphics.drawCenteredString(this.font, homeLabel, this.width / 2, homeLabelY, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
