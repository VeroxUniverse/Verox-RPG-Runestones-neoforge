package net.veroxuniverse.verox_rpg_runestones.world;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.veroxuniverse.verox_rpg_runestones.registry.ModStructures;

import java.util.Comparator;
import java.util.Optional;

public class UniqueRunestonePoolElement extends SinglePoolElement {

    public static final MapCodec<UniqueRunestonePoolElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            templateCodec(),
            processorsCodec(),
            projectionCodec(),
            overrideLiquidSettingsCodec()
    ).apply(instance, UniqueRunestonePoolElement::new));

    private static final Comparator<BlockPos> ORDER = Comparator.comparingInt((BlockPos pos) -> pos.getX())
            .thenComparingInt(pos -> pos.getZ())
            .thenComparingInt(pos -> pos.getY());

    public UniqueRunestonePoolElement(Either<ResourceLocation, StructureTemplate> template, Holder<StructureProcessorList> processors,
                                      StructureTemplatePool.Projection projection, Optional<LiquidSettings> overrideLiquidSettings) {
        super(template, processors, projection, overrideLiquidSettings);
    }

    @Override
    public boolean place(StructureTemplateManager templateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                         BlockPos offset, BlockPos pos, Rotation rotation, BoundingBox box, RandomSource random, LiquidSettings liquidSettings,
                         boolean keepJigsaws) {
        if (!this.isFirstInStructure(structureManager, offset)) {
            return true;
        }
        return super.place(templateManager, level, structureManager, generator, offset, pos, rotation, box, random, liquidSettings, keepJigsaws);
    }

    private boolean isFirstInStructure(StructureManager structureManager, BlockPos piecePosition) {
        StructureStart start = structureManager.getStructureWithPieceAt(piecePosition, structure -> true);
        if (!start.isValid()) return true;

        BlockPos first = null;
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof PoolElementStructurePiece poolPiece && poolPiece.getElement() instanceof UniqueRunestonePoolElement) {
                BlockPos candidate = poolPiece.getPosition();
                if (first == null || ORDER.compare(candidate, first) < 0) {
                    first = candidate;
                }
            }
        }
        return first == null || first.equals(piecePosition);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return ModStructures.UNIQUE_POOL_ELEMENT.get();
    }

    @Override
    public String toString() {
        return "UniqueRunestone[" + this.template + "]";
    }
}