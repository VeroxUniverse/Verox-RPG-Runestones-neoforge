package net.veroxuniverse.verox_rpg_runestones.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RunestoneRegistry extends SavedData {

    private static final String DATA_NAME = RPGRunestones.MOD_ID + "_registry";
    private static final String ENTRIES_KEY = "entries";

    private final Map<UUID, RunestoneEntry> entries = new LinkedHashMap<>();

    public static RunestoneRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(RunestoneRegistry::new, RunestoneRegistry::load, null), DATA_NAME);
    }

    private static RunestoneRegistry load(CompoundTag tag, HolderLookup.Provider provider) {
        RunestoneRegistry registry = new RunestoneRegistry();
        if (tag.contains(ENTRIES_KEY)) {
            RunestoneEntry.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get(ENTRIES_KEY))
                    .resultOrPartial(error -> RPGRunestones.LOGGER.error("Failed to load runestones: {}", error))
                    .ifPresent(list -> list.forEach(entry -> registry.entries.put(entry.id(), entry)));
        }
        return registry;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        RunestoneEntry.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.entries.values().stream().toList())
                .resultOrPartial(error -> RPGRunestones.LOGGER.error("Failed to save runestones: {}", error))
                .ifPresent(encoded -> tag.put(ENTRIES_KEY, encoded));
        return tag;
    }

    public void add(RunestoneEntry entry) {
        this.entries.put(entry.id(), entry);
        this.setDirty();
    }

    public Optional<RunestoneEntry> get(UUID id) {
        return Optional.ofNullable(this.entries.get(id));
    }

    public Optional<RunestoneEntry> findAt(ResourceKey<Level> dimension, BlockPos pos) {
        for (RunestoneEntry entry : this.entries.values()) {
            if (entry.dimension().equals(dimension) && entry.pos().equals(pos)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public void rename(UUID id, String name) {
        RunestoneEntry entry = this.entries.get(id);
        if (entry == null) return;
        this.entries.put(id, entry.withName(name));
        this.setDirty();
    }

    public void setGlobal(UUID id, boolean global) {
        RunestoneEntry entry = this.entries.get(id);
        if (entry == null || entry.global() == global) return;
        this.entries.put(id, entry.withGlobal(global));
        this.setDirty();
    }

    public void remove(UUID id) {
        if (this.entries.remove(id) != null) {
            this.setDirty();
        }
    }

    public Collection<RunestoneEntry> all() {
        return this.entries.values();
    }
}
