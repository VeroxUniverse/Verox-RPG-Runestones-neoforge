package net.veroxuniverse.verox_rpg_runestones.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record RunestoneEntry(UUID id, String name, ResourceKey<Level> dimension, BlockPos pos, UUID owner, boolean global) {

    public static final Codec<RunestoneEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(RunestoneEntry::id),
            Codec.STRING.fieldOf("name").forGetter(RunestoneEntry::name),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(RunestoneEntry::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(RunestoneEntry::pos),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(RunestoneEntry::owner),
            Codec.BOOL.optionalFieldOf("global", false).forGetter(RunestoneEntry::global)
    ).apply(instance, RunestoneEntry::new));

    public RunestoneEntry withName(String newName) {
        return new RunestoneEntry(this.id, newName, this.dimension, this.pos, this.owner, this.global);
    }

    public RunestoneEntry withGlobal(boolean newGlobal) {
        return new RunestoneEntry(this.id, this.name, this.dimension, this.pos, this.owner, newGlobal);
    }
}
