package net.veroxuniverse.verox_rpg_runestones.client.render;

import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class RunestoneGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

    private static final ResourceLocation MODEL = RPGRunestones.id("geo/runestone.geo.json");
    private static final ResourceLocation TEXTURE = RPGRunestones.id("textures/block/runestone.png");
    private static final ResourceLocation ANIMATIONS = RPGRunestones.id("animations/runestone.animation.json");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATIONS;
    }
}
