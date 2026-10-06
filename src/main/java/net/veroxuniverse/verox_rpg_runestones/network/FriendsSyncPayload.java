package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.List;

public record FriendsSyncPayload(
        List<FriendInfo> friends,
        List<FriendInfo> incoming,
        List<FriendInfo> outgoing,
        String noticeKey,
        String noticeArg
) implements CustomPacketPayload {

    public static final Type<FriendsSyncPayload> TYPE = new Type<>(RPGRunestones.id("friends_sync"));

    public static final StreamCodec<ByteBuf, FriendsSyncPayload> STREAM_CODEC = StreamCodec.composite(
            FriendInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), FriendsSyncPayload::friends,
            FriendInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), FriendsSyncPayload::incoming,
            FriendInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), FriendsSyncPayload::outgoing,
            ByteBufCodecs.STRING_UTF8, FriendsSyncPayload::noticeKey,
            ByteBufCodecs.STRING_UTF8, FriendsSyncPayload::noticeArg,
            FriendsSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
