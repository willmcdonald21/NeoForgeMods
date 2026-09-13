package com.example.villagerpaths.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** The world's shared collection of named destination zones. Stored once on the overworld, regardless of dimension. */
public record ZoneLibrary(List<NamedZone> zones) {
    public static final ZoneLibrary EMPTY = new ZoneLibrary(List.of());

    public static final Codec<ZoneLibrary> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NamedZone.CODEC.listOf().fieldOf("zones").forGetter(ZoneLibrary::zones)
    ).apply(instance, ZoneLibrary::new));

    public Optional<NamedZone> find(UUID id) {
        return zones.stream().filter(z -> z.id().equals(id)).findFirst();
    }

    public ZoneLibrary withZone(NamedZone updated) {
        List<NamedZone> next = new ArrayList<>(zones.size() + 1);
        boolean replaced = false;
        for (NamedZone existing : zones) {
            if (existing.id().equals(updated.id())) {
                next.add(updated);
                replaced = true;
            } else {
                next.add(existing);
            }
        }
        if (!replaced) {
            next.add(updated);
        }
        return new ZoneLibrary(List.copyOf(next));
    }

    public ZoneLibrary withoutZone(UUID id) {
        List<NamedZone> next = new ArrayList<>(zones.size());
        for (NamedZone existing : zones) {
            if (!existing.id().equals(id)) {
                next.add(existing);
            }
        }
        return new ZoneLibrary(List.copyOf(next));
    }
}
