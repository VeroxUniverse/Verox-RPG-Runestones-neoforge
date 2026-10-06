package net.veroxuniverse.verox_rpg_runestones.world;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class VillageRunestones {

    private static final ResourceLocation PIECE = RPGRunestones.id("ancient_runestone");
    private static final int WEIGHT = 1;
    private static final List<String> VILLAGE_TYPES = List.of("plains", "desert", "savanna", "snowy", "taiga");
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSORS =
            ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("empty"));

    private VillageRunestones() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> pools = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processors = event.getServer().registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> empty = processors.getHolderOrThrow(EMPTY_PROCESSORS);

        for (String type : VILLAGE_TYPES) {
            addPiece(pools, empty, ResourceLocation.withDefaultNamespace("village/" + type + "/houses"));
        }
    }

    private static void addPiece(Registry<StructureTemplatePool> pools, Holder<StructureProcessorList> processors, ResourceLocation poolId) {
        StructureTemplatePool pool = pools.get(poolId);
        if (pool == null) return;

        UniqueRunestonePoolElement piece = new UniqueRunestonePoolElement(Either.left(PIECE), processors,
                StructureTemplatePool.Projection.RIGID, Optional.empty());
        for (int i = 0; i < WEIGHT; i++) {
            pool.templates.add(piece);
        }
        List<Pair<StructurePoolElement, Integer>> entries = new ArrayList<>(pool.rawTemplates);
        entries.add(Pair.of(piece, WEIGHT));
        pool.rawTemplates = entries;
    }
}
