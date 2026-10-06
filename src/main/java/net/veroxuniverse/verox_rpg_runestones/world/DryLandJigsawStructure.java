package net.veroxuniverse.verox_rpg_runestones.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.veroxuniverse.verox_rpg_runestones.registry.ModStructures;

import java.util.Optional;

public class DryLandJigsawStructure extends Structure {

    public static final MapCodec<DryLandJigsawStructure> CODEC = JigsawStructure.CODEC.xmap(DryLandJigsawStructure::new, structure -> structure.jigsaw);

    private static final int CHECK_RADIUS = 5;
    private static final int[][] CHECK_OFFSETS = {{0, 0}, {CHECK_RADIUS, 0}, {-CHECK_RADIUS, 0}, {0, CHECK_RADIUS}, {0, -CHECK_RADIUS}};

    private final JigsawStructure jigsaw;

    public DryLandJigsawStructure(JigsawStructure jigsaw) {
        super(new StructureSettings(jigsaw.biomes(), jigsaw.spawnOverrides(), jigsaw.step(), jigsaw.terrainAdaptation()));
        this.jigsaw = jigsaw;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int centerX = chunk.getMiddleBlockX();
        int centerZ = chunk.getMiddleBlockZ();

        for (int[] offset : CHECK_OFFSETS) {
            int x = centerX + offset[0];
            int z = centerZ + offset[1];
            int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int floor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
            if (surface != floor) {
                return Optional.empty();
            }
        }
        return this.jigsaw.findValidGenerationPoint(context);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DRY_LAND_JIGSAW.get();
    }
}
