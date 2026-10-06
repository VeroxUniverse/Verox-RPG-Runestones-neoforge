package net.veroxuniverse.verox_rpg_runestones.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import net.veroxuniverse.verox_rpg_runestones.client.ClientRunestones;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class RunestoneDormantLayer extends GeoRenderLayer<RunestoneBlockEntity> {

    private static final ResourceLocation TEXTURE = RPGRunestones.id("textures/block/runestone_dormant.png");
    private static final int WHITE = 0xFFFFFFFF;

    public RunestoneDormantLayer(GeoRenderer<RunestoneBlockEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, RunestoneBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (ClientRunestones.knows(animatable)) return;

        RenderType dormantType = RenderType.entityCutoutNoCull(TEXTURE);
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, dormantType,
                bufferSource.getBuffer(dormantType), partialTick, packedLight, packedOverlay, WHITE);
    }
}
