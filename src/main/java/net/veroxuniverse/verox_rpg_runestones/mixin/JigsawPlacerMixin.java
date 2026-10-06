package net.veroxuniverse.verox_rpg_runestones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.veroxuniverse.verox_rpg_runestones.world.VillagePavilionRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class JigsawPlacerMixin {

    @Shadow
    @Final
    private List<? super PoolElementStructurePiece> pieces;

    @WrapOperation(
            method = "tryPlacingChildren",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getShuffledTemplates(Lnet/minecraft/util/RandomSource;)Ljava/util/List;")
    )
    private List<StructurePoolElement> verox_rpg_runestones$onePavilionPerVillage(StructureTemplatePool pool, RandomSource random, Operation<List<StructurePoolElement>> original) {
        return VillagePavilionRules.apply(original.call(pool, random), this.pieces);
    }
}
