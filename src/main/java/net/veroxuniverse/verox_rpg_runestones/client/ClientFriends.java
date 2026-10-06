package net.veroxuniverse.verox_rpg_runestones.client;

import net.veroxuniverse.verox_rpg_runestones.network.FriendsSyncPayload;

import java.util.List;

public final class ClientFriends {

    private static FriendsSyncPayload latest = new FriendsSyncPayload(List.of(), List.of(), List.of(), "", "");

    private ClientFriends() {}

    public static FriendsSyncPayload get() {
        return latest;
    }

    public static void update(FriendsSyncPayload payload) {
        latest = payload;
    }
}
