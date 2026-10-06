package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.UUID;

public record FriendActionPayload(Action action, UUID target, String name) implements CustomPacketPayload {

    public enum Action {
        SYNC, REQUEST, ACCEPT, DECLINE, CANCEL, REMOVE;

        public static final StreamCodec<ByteBuf, Action> STREAM_CODEC =
                ByteBufCodecs.VAR_INT.map(index -> values()[index], Action::ordinal);
    }

    public static final Type<FriendActionPayload> TYPE = new Type<>(RPGRunestones.id("friend_action"));

    public static final StreamCodec<ByteBuf, FriendActionPayload> STREAM_CODEC = StreamCodec.composite(
            Action.STREAM_CODEC, FriendActionPayload::action,
            UUIDUtil.STREAM_CODEC, FriendActionPayload::target,
            ByteBufCodecs.STRING_UTF8, FriendActionPayload::name,
            FriendActionPayload::new
    );

    public static FriendActionPayload of(Action action, UUID target) {
        return new FriendActionPayload(action, target, "");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
