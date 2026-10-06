package net.veroxuniverse.verox_rpg_runestones.friends;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.network.FriendActionPayload;
import net.veroxuniverse.verox_rpg_runestones.network.FriendInfo;
import net.veroxuniverse.verox_rpg_runestones.network.FriendsSyncPayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class FriendService {

    private static final String NOTICE = "gui." + RPGRunestones.MOD_ID + ".friends.notice.";
    private static final int MAX_NAME_LENGTH = 16;

    private FriendService() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FriendsData data = FriendsData.get(player.server);
        data.rememberName(player.getUUID(), player.getGameProfile().getName());

        int pending = data.incoming(player.getUUID()).size();
        if (pending > 0) {
            player.sendSystemMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".friend_requests_pending", pending)
                    .withStyle(ChatFormatting.AQUA));
        }
        sync(player, "", "");
    }

    public static void handleAction(ServerPlayer player, FriendActionPayload payload) {
        MinecraftServer server = player.server;
        FriendsData data = FriendsData.get(server);
        UUID self = player.getUUID();
        data.rememberName(self, player.getGameProfile().getName());

        switch (payload.action()) {
            case SYNC -> sync(player, "", "");
            case REQUEST -> request(player, data, payload.name().trim());
            case ACCEPT -> {
                if (!data.hasRequest(payload.target(), self)) {
                    sync(player, "", "");
                    return;
                }
                data.addFriendship(self, payload.target());
                String name = data.nameOf(payload.target());
                sync(player, "accepted", name);
                notifyOther(server, payload.target(), "accepted", data.nameOf(self));
            }
            case DECLINE -> {
                data.removeRequest(payload.target(), self);
                sync(player, "declined", data.nameOf(payload.target()));
                notifyOther(server, payload.target(), "", "");
            }
            case CANCEL -> {
                data.removeRequest(self, payload.target());
                sync(player, "cancelled", data.nameOf(payload.target()));
                notifyOther(server, payload.target(), "", "");
            }
            case REMOVE -> {
                data.removeFriendship(self, payload.target());
                sync(player, "removed", data.nameOf(payload.target()));
                notifyOther(server, payload.target(), "", "");
            }
        }
    }

    private static void request(ServerPlayer player, FriendsData data, String name) {
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            sync(player, "not_found", name);
            return;
        }

        Optional<GameProfile> profile = resolve(player.server, data, name);
        if (profile.isEmpty()) {
            sync(player, "not_found", name);
            return;
        }

        UUID self = player.getUUID();
        UUID target = profile.get().getId();
        String targetName = profile.get().getName();
        data.rememberName(target, targetName);

        if (target.equals(self)) {
            sync(player, "self", targetName);
        } else if (data.areFriends(self, target)) {
            sync(player, "already", targetName);
        } else if (data.hasRequest(target, self)) {
            data.addFriendship(self, target);
            sync(player, "accepted", targetName);
            notifyOther(player.server, target, "accepted", player.getGameProfile().getName());
        } else if (data.hasRequest(self, target)) {
            sync(player, "pending", targetName);
        } else {
            data.addRequest(self, target);
            sync(player, "sent", targetName);
            ServerPlayer other = player.server.getPlayerList().getPlayer(target);
            if (other != null) {
                other.sendSystemMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".friend_request_received",
                        player.getGameProfile().getName()).withStyle(ChatFormatting.AQUA));
                sync(other, "received", player.getGameProfile().getName());
            }
        }
    }

    private static Optional<GameProfile> resolve(MinecraftServer server, FriendsData data, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) return Optional.of(online.getGameProfile());

        Optional<UUID> known = data.findByName(name);
        if (known.isPresent()) return Optional.of(new GameProfile(known.get(), data.nameOf(known.get())));

        if (server.getProfileCache() != null) {
            return server.getProfileCache().get(name);
        }
        return Optional.empty();
    }

    private static void notifyOther(MinecraftServer server, UUID target, String noticeKey, String noticeArg) {
        ServerPlayer other = server.getPlayerList().getPlayer(target);
        if (other != null) {
            sync(other, noticeKey, noticeArg);
        }
    }

    public static void sync(ServerPlayer player, String noticeKey, String noticeArg) {
        FriendsData data = FriendsData.get(player.server);
        UUID self = player.getUUID();
        PacketDistributor.sendToPlayer(player, new FriendsSyncPayload(
                infos(player.server, data, data.friendsOf(self)),
                infos(player.server, data, data.incoming(self)),
                infos(player.server, data, data.outgoing(self)),
                noticeKey.isEmpty() ? "" : NOTICE + noticeKey,
                noticeArg));
    }

    private static List<FriendInfo> infos(MinecraftServer server, FriendsData data, Set<UUID> ids) {
        List<FriendInfo> result = new ArrayList<>();
        for (UUID id : ids) {
            if (id.equals(Util.NIL_UUID)) continue;
            result.add(new FriendInfo(id, data.nameOf(id), server.getPlayerList().getPlayer(id) != null));
        }
        result.sort(Comparator.comparing(FriendInfo::online).reversed().thenComparing(info -> info.name().toLowerCase(Locale.ROOT)));
        return result;
    }
}
