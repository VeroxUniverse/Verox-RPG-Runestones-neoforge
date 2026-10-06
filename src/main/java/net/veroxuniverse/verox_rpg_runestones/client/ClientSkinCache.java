package net.veroxuniverse.verox_rpg_runestones.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientSkinCache {

    private static final Map<UUID, PlayerSkin> SKINS = new ConcurrentHashMap<>();
    private static final Set<UUID> REQUESTED = ConcurrentHashMap.newKeySet();

    private ClientSkinCache() {}

    public static PlayerSkin get(UUID id) {
        PlayerSkin cached = SKINS.get(id);
        if (cached != null) return cached;

        if (REQUESTED.add(id)) {
            Minecraft minecraft = Minecraft.getInstance();
            CompletableFuture.supplyAsync(() -> minecraft.getMinecraftSessionService().fetchProfile(id, false), Util.backgroundExecutor())
                    .thenCompose(result -> result == null
                            ? CompletableFuture.<PlayerSkin>completedFuture(null)
                            : minecraft.getSkinManager().getOrLoad(result.profile()))
                    .thenAccept(skin -> {
                        if (skin != null) SKINS.put(id, skin);
                    })
                    .exceptionally(error -> null);
        }
        return DefaultPlayerSkin.get(id);
    }

}
