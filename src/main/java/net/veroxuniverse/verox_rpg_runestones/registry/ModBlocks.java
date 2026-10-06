package net.veroxuniverse.verox_rpg_runestones.registry;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneBlock;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RPGRunestones.MOD_ID);

    public static final DeferredBlock<RunestoneBlock> RUNESTONE = BLOCKS.register("runestone",
            () -> new RunestoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .strength(3.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .pushReaction(PushReaction.BLOCK)
                    .noOcclusion()
                    .forceSolidOn()));

    private ModBlocks() {}
}
