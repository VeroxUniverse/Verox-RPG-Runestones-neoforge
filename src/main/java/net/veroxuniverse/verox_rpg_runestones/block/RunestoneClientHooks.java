package net.veroxuniverse.verox_rpg_runestones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.function.BiPredicate;
import java.util.function.Function;

public final class RunestoneClientHooks {

    public static final RawAnimation DORMANT = RawAnimation.begin().thenLoop("animation.runestone.dormant");
    public static final RawAnimation AWAKEN = RawAnimation.begin().thenPlay("animation.runestone.awaken").thenLoop("animation.runestone.idle");
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.runestone.idle");
    public static final RawAnimation ACTIVE = RawAnimation.begin().thenLoop("animation.runestone.active");
    public static final RawAnimation TELEPORT = RawAnimation.begin().thenPlay("animation.runestone.teleport");
    public static final RawAnimation ARRIVE = RawAnimation.begin().thenPlay("animation.runestone.arrive");

    public static BiPredicate<BlockGetter, BlockPos> dormantCheck = (level, lowerPos) -> false;
    public static Function<RunestoneBlockEntity, RawAnimation> animationSelector = blockEntity -> IDLE;

    private RunestoneClientHooks() {}
}
