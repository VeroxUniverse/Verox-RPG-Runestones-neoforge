package net.veroxuniverse.verox_rpg_runestones.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.veroxuniverse.verox_rpg_runestones.registry.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RUNE_DUST.get(), 3)
                .requires(Items.AMETHYST_SHARD)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RUNESTONE.get())
                .pattern("#A#")
                .pattern("ADA")
                .pattern("#A#")
                .define('#', Items.SMOOTH_STONE)
                .define('A', Items.AMETHYST_SHARD)
                .define('D', ModItems.RUNE_TABLET.get())
                .unlockedBy("has_rune_dust", has(ModItems.RUNE_DUST.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.RUNE_TABLET.get())
                .pattern("EDE")
                .pattern("DSD")
                .pattern("EDE")
                .define('D', ModItems.RUNE_DUST.get())
                .define('S', Items.ECHO_SHARD)
                .define('E', Items.ENDER_PEARL)
                .unlockedBy("has_rune_dust", has(ModItems.RUNE_DUST.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SOUL_ANCHOR.get())
                .pattern(" C ")
                .pattern("DED")
                .pattern(" F ")
                .define('C', Items.CHAIN)
                .define('E', Items.ENDER_EYE)
                .define('F', Items.ECHO_SHARD)
                .define('D', ModItems.RUNE_DUST.get())
                .unlockedBy("has_rune_dust", has(ModItems.RUNE_DUST.get()))
                .save(output);
    }
}
