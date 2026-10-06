package net.veroxuniverse.verox_rpg_runestones.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, RPGRunestones.MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        this.flatItem("rune_dust");
        this.flatItem("rune_tablet");
        this.flatItem("soul_anchor");
    }

    private void flatItem(String name) {
        this.withExistingParent(name, this.mcLoc("item/generated")).texture("layer0", this.modLoc("item/" + name));
    }
}