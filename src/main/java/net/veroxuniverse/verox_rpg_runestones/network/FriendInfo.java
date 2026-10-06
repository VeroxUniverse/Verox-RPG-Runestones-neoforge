package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record FriendInfo(UUID id, String name, boolean online) {

    public static final StreamCodec<ByteBuf, FriendInfo> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FriendInfo::id,
            ByteBufCodecs.STRING_UTF8, FriendInfo::name,
            ByteBufCodecs.BOOL, FriendInfo::online,
            FriendInfo::new
    );
}
