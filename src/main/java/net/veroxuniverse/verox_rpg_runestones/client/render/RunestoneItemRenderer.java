package net.veroxuniverse.verox_rpg_runestones.client.render;

import net.veroxuniverse.verox_rpg_runestones.item.RunestoneItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class RunestoneItemRenderer extends GeoItemRenderer<RunestoneItem> {

    public RunestoneItemRenderer() {
        super(new RunestoneGeoModel<>());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}