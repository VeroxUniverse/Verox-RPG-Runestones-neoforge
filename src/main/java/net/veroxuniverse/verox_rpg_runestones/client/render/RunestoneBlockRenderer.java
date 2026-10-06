package net.veroxuniverse.verox_rpg_runestones.client.render;

import net.minecraft.world.phys.AABB;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RunestoneBlockRenderer extends GeoBlockRenderer<RunestoneBlockEntity> {

    private static final double RENDER_RADIUS = 5.0;

    public RunestoneBlockRenderer() {
        super(new RunestoneGeoModel<>());
        this.addRenderLayer(new RunestoneDormantLayer(this));
        this.addRenderLayer(new RunestoneGlowLayer(this));
    }

    @Override
    public AABB getRenderBoundingBox(RunestoneBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(RENDER_RADIUS);
    }
}
