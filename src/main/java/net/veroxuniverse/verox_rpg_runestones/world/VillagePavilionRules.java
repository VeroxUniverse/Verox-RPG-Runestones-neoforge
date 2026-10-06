package net.veroxuniverse.verox_rpg_runestones.world;

import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

import java.util.ArrayList;
import java.util.List;

public final class VillagePavilionRules {

    private VillagePavilionRules() {}

    public static List<StructurePoolElement> apply(List<StructurePoolElement> templates, List<?> placedPieces) {
        StructurePoolElement pavilion = null;
        for (StructurePoolElement element : templates) {
            if (element instanceof UniqueRunestonePoolElement) {
                pavilion = element;
                break;
            }
        }
        if (pavilion == null) return templates;

        List<StructurePoolElement> result = new ArrayList<>(templates.size());
        if (!hasPavilion(placedPieces)) {
            result.add(pavilion);
        }
        for (StructurePoolElement element : templates) {
            if (!(element instanceof UniqueRunestonePoolElement)) {
                result.add(element);
            }
        }
        return result;
    }

    private static boolean hasPavilion(List<?> placedPieces) {
        for (Object piece : placedPieces) {
            if (piece instanceof PoolElementStructurePiece poolPiece && poolPiece.getElement() instanceof UniqueRunestonePoolElement) {
                return true;
            }
        }
        return false;
    }
}
