package com.example.villagerpaths.attachment;

import java.util.function.Supplier;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.example.villagerpaths.VillagerPathsMod;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, VillagerPathsMod.MODID);

    public static final Supplier<AttachmentType<VillagerPathData>> VILLAGER_PATH = ATTACHMENT_TYPES.register(
            "villager_path",
            () -> AttachmentType.builder(() -> VillagerPathData.EMPTY)
                    .serialize(VillagerPathData.CODEC)
                    .build());

    public static final Supplier<AttachmentType<VillagerHomeData>> VILLAGER_HOME = ATTACHMENT_TYPES.register(
            "villager_home",
            () -> AttachmentType.builder(() -> VillagerHomeData.EMPTY)
                    .serialize(VillagerHomeData.CODEC)
                    .build());

    public static final Supplier<AttachmentType<PathLibrary>> PATH_LIBRARY = ATTACHMENT_TYPES.register(
            "path_library",
            () -> AttachmentType.builder(() -> PathLibrary.EMPTY)
                    .serialize(PathLibrary.CODEC)
                    .build());
}
