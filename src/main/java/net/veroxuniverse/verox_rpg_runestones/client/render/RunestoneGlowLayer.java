package net.veroxuniverse.verox_rpg_runestones.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import net.veroxuniverse.verox_rpg_runestones.client.ClientRunestones;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class RunestoneGlowLayer extends AutoGlowingGeoLayer<RunestoneBlockEntity> {

    public RunestoneGlowLayer(GeoRenderer<RunestoneBlockEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, RunestoneBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!ClientRunestones.knows(animatable)) return;
        super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}