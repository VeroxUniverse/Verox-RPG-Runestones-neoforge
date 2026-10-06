package net.veroxuniverse.verox_rpg_runestones.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RPGRunestones.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RunestoneBlockEntity>> RUNESTONE =
            BLOCK_ENTITIES.register("runestone",
                    () -> BlockEntityType.Builder.of(RunestoneBlockEntity::new, ModBlocks.RUNESTONE.get()).build(null));

    private ModBlockEntities() {}
}
