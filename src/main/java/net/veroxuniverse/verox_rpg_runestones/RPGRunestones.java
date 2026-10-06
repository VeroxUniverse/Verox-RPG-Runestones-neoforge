package net.veroxuniverse.verox_rpg_runestones;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.veroxuniverse.verox_rpg_runestones.config.RunestonesConfig;
import net.veroxuniverse.verox_rpg_runestones.registry.ModAttachments;
import net.veroxuniverse.verox_rpg_runestones.registry.ModBlockEntities;
import net.veroxuniverse.verox_rpg_runestones.registry.ModBlocks;
import net.veroxuniverse.verox_rpg_runestones.registry.ModCreativeTabs;
import net.veroxuniverse.verox_rpg_runestones.registry.ModItems;
import org.slf4j.Logger;

@Mod(RPGRunestones.MOD_ID)
public class RPGRunestones {
    public static final String MOD_ID = "verox_rpg_runestones";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RPGRunestones(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, RunestonesConfig.SPEC);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
