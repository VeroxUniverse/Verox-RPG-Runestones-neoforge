package net.veroxuniverse.verox_rpg_runestones.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public class RunestoneNames extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String FALLBACK = "Runestone";
    private static final int MAX_LENGTH = 20;

    private static volatile List<String> names = List.of();

    public RunestoneNames() {
        super(GSON, "runestone_names");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        Set<String> loaded = new LinkedHashSet<>();
        for (Map.Entry<ResourceLocation, JsonElement> file : new TreeMap<>(files).entrySet()) {
            if (!file.getValue().isJsonObject()) continue;
            JsonObject object = file.getValue().getAsJsonObject();
            if (object.has("replace") && object.get("replace").getAsBoolean()) {
                loaded.clear();
            }
            if (!object.has("names") || !object.get("names").isJsonArray()) continue;

            JsonArray array = object.getAsJsonArray("names");
            for (JsonElement element : array) {
                String name = element.getAsString().trim();
                if (!name.isEmpty() && name.length() <= MAX_LENGTH) {
                    loaded.add(name);
                }
            }
        }
        names = List.copyOf(loaded);
        RPGRunestones.LOGGER.info("Loaded {} runestone names.", names.size());
    }

    public static String pick(RandomSource random, Collection<String> usedNames) {
        List<String> pool = names;
        if (pool.isEmpty()) return FALLBACK;

        List<String> unused = new ArrayList<>();
        for (String name : pool) {
            if (!usedNames.contains(name)) unused.add(name);
        }
        List<String> source = unused.isEmpty() ? pool : unused;
        return source.get(random.nextInt(source.size()));
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new RunestoneNames());
    }
}
