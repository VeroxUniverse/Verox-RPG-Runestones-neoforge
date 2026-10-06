package net.veroxuniverse.verox_rpg_runestones.compat.jade;

import net.veroxuniverse.verox_rpg_runestones.block.RunestoneBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class RunestonesJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(RunestoneComponentProvider.INSTANCE, RunestoneBlock.class);
    }
}
