package net.veroxuniverse.verox_rpg_runestones.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PlayerRunestones(List<UUID> known, long tabletReadyAt, List<UUID> granted) {

    public static final PlayerRunestones EMPTY = new PlayerRunestones(List.of(), 0L, List.of());

    public static final Codec<PlayerRunestones> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.listOf().optionalFieldOf("known", List.of()).forGetter(PlayerRunestones::known),
            Codec.LONG.optionalFieldOf("tablet_ready_at", 0L).forGetter(PlayerRunestones::tabletReadyAt),
            UUIDUtil.CODEC.listOf().optionalFieldOf("granted", List.of()).forGetter(PlayerRunestones::granted)
    ).apply(instance, PlayerRunestones::new));

    public boolean knows(UUID id) {
        return this.known.contains(id);
    }

    public PlayerRunestones withKnown(UUID id) {
        List<UUID> granted = new ArrayList<>(this.granted);
        boolean wasGranted = granted.remove(id);
        if (this.knows(id)) {
            return wasGranted ? new PlayerRunestones(this.known, this.tabletReadyAt, List.copyOf(granted)) : this;
        }
        List<UUID> updated = new ArrayList<>(this.known);
        updated.add(id);
        return new PlayerRunestones(List.copyOf(updated), this.tabletReadyAt, List.copyOf(granted));
    }

    public PlayerRunestones withGranted(UUID id) {
        if (this.knows(id)) return this;
        List<UUID> updated = new ArrayList<>(this.known);
        updated.add(id);
        List<UUID> granted = new ArrayList<>(this.granted);
        granted.add(id);
        return new PlayerRunestones(List.copyOf(updated), this.tabletReadyAt, List.copyOf(granted));
    }

    public PlayerRunestones withoutGranted(UUID id) {
        if (!this.granted.contains(id)) return this;
        List<UUID> updated = new ArrayList<>(this.known);
        updated.remove(id);
        List<UUID> granted = new ArrayList<>(this.granted);
        granted.remove(id);
        return new PlayerRunestones(List.copyOf(updated), this.tabletReadyAt, List.copyOf(granted));
    }

    public PlayerRunestones withSwapped(UUID first, UUID second) {
        int a = this.known.indexOf(first);
        int b = this.known.indexOf(second);
        if (a < 0 || b < 0 || a == b) return this;
        List<UUID> updated = new ArrayList<>(this.known);
        updated.set(a, second);
        updated.set(b, first);
        return new PlayerRunestones(List.copyOf(updated), this.tabletReadyAt, this.granted);
    }

    public PlayerRunestones withMovedToEnd(UUID id) {
        if (!this.knows(id)) return this;
        List<UUID> updated = new ArrayList<>(this.known);
        updated.remove(id);
        updated.add(id);
        return new PlayerRunestones(List.copyOf(updated), this.tabletReadyAt, this.granted);
    }

    public PlayerRunestones withTabletReadyAt(long gameTime) {
        return new PlayerRunestones(this.known, gameTime, this.granted);
    }
}
