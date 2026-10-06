package net.veroxuniverse.verox_rpg_runestones.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.registry.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, RPGRunestones.MOD_ID, fileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile particleOnly = this.models().getBuilder("runestone")
                .texture("particle", this.modLoc("block/runestone"));

        this.getVariantBuilder(ModBlocks.RUNESTONE.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(particleOnly).build());
    }
}
