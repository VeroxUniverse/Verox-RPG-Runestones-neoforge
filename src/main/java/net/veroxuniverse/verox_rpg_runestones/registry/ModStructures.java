package net.veroxuniverse.verox_rpg_runestones.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.world.DryLandJigsawStructure;
import net.veroxuniverse.verox_rpg_runestones.world.UniqueRunestonePoolElement;

public final class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, RPGRunestones.MOD_ID);
    public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, RPGRunestones.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<DryLandJigsawStructure>> DRY_LAND_JIGSAW =
            STRUCTURE_TYPES.register("dry_land_jigsaw", () -> () -> DryLandJigsawStructure.CODEC);

    public static final DeferredHolder<StructurePoolElementType<?>, StructurePoolElementType<UniqueRunestonePoolElement>> UNIQUE_POOL_ELEMENT =
            POOL_ELEMENT_TYPES.register("unique_single", () -> () -> UniqueRunestonePoolElement.CODEC);

    private ModStructures() {}
}
