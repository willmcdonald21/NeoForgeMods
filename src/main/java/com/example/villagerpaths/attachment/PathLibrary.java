package com.example.villagerpaths.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** The world's shared collection of named paths. Stored once on the overworld, regardless of dimension. */
public record PathLibrary(List<NamedPath> paths) {
    public static final PathLibrary EMPTY = new PathLibrary(List.of());

    public static final Codec<PathLibrary> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NamedPath.CODEC.listOf().fieldOf("paths").forGetter(PathLibrary::paths)
    ).apply(instance, PathLibrary::new));

    public Optional<NamedPath> find(UUID id) {
        return paths.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    public PathLibrary withPath(NamedPath updated) {
        List<NamedPath> next = new ArrayList<>(paths.size() + 1);
        boolean replaced = false;
        for (NamedPath existing : paths) {
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
        return new PathLibrary(List.copyOf(next));
    }

    public PathLibrary withoutPath(UUID id) {
        List<NamedPath> next = new ArrayList<>(paths.size());
        for (NamedPath existing : paths) {
            if (!existing.id().equals(id)) {
                next.add(existing);
            }
        }
        return new PathLibrary(List.copyOf(next));
    }
}
