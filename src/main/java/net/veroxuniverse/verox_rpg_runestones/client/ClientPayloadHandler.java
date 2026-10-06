package net.veroxuniverse.verox_rpg_runestones.client;

import net.minecraft.client.Minecraft;
import net.veroxuniverse.verox_rpg_runestones.client.screen.FriendsScreen;
import net.veroxuniverse.verox_rpg_runestones.client.screen.RunestoneMenuScreen;
import net.veroxuniverse.verox_rpg_runestones.client.screen.RunestoneEditScreen;
import net.veroxuniverse.verox_rpg_runestones.network.FriendsSyncPayload;
import net.veroxuniverse.verox_rpg_runestones.network.KnownRunestonesPayload;
import net.veroxuniverse.verox_rpg_runestones.network.OpenNamingScreenPayload;
import net.veroxuniverse.verox_rpg_runestones.network.OpenRunestoneMenuPayload;

public final class ClientPayloadHandler {

    private ClientPayloadHandler() {}

    public static void openMenu(OpenRunestoneMenuPayload payload) {
        Minecraft.getInstance().setScreen(new RunestoneMenuScreen(payload));
    }

    public static void knownRunestones(KnownRunestonesPayload payload) {
        ClientRunestones.update(payload.known());
    }

    public static void friendsSync(FriendsSyncPayload payload) {
        ClientFriends.update(payload);
        if (Minecraft.getInstance().screen instanceof FriendsScreen screen) {
            screen.onSync(payload);
        }
    }

    public static void openNaming(OpenNamingScreenPayload payload) {
        Minecraft.getInstance().setScreen(new RunestoneEditScreen(payload.id(), payload.name(), payload.global(), payload.canSetGlobal(), false));
    }
}
