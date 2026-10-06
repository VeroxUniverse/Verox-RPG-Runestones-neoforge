package net.veroxuniverse.verox_rpg_runestones.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.client.ClientPayloadHandler;
import net.veroxuniverse.verox_rpg_runestones.friends.FriendService;
import net.veroxuniverse.verox_rpg_runestones.teleport.RunestoneService;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(OpenRunestoneMenuPayload.TYPE, OpenRunestoneMenuPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPayloadHandler.openMenu(payload)));
        registrar.playToClient(OpenNamingScreenPayload.TYPE, OpenNamingScreenPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPayloadHandler.openNaming(payload)));

        registrar.playToClient(KnownRunestonesPayload.TYPE, KnownRunestonesPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPayloadHandler.knownRunestones(payload)));
        registrar.playToClient(FriendsSyncPayload.TYPE, FriendsSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPayloadHandler.friendsSync(payload)));

        registrar.playToServer(FriendActionPayload.TYPE, FriendActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        FriendService.handleAction(player, payload);
                    }
                }));
        registrar.playToServer(TeleportRequestPayload.TYPE, TeleportRequestPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        RunestoneService.handleTeleport(player, payload);
                    }
                }));
        registrar.playToServer(ReorderRunestonesPayload.TYPE, ReorderRunestonesPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        RunestoneService.handleReorder(player, payload);
                    }
                }));
        registrar.playToServer(RenameRunestonePayload.TYPE, RenameRunestonePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        RunestoneService.handleRename(player, payload);
                    }
                }));
    }
}
