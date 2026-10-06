package net.veroxuniverse.verox_rpg_runestones.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneClientHooks;
import net.veroxuniverse.verox_rpg_runestones.client.render.RunestoneBlockRenderer;
import net.veroxuniverse.verox_rpg_runestones.registry.ModBlockEntities;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RunestoneClientHooks.dormantCheck = ClientRunestones::isDormant;
        RunestoneClientHooks.animationSelector = ClientRunestones::selectAnimation;
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RUNESTONE.get(), context -> new RunestoneBlockRenderer());
    }
}
