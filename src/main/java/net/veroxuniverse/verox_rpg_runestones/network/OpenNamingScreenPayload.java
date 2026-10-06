package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.UUID;

public record OpenNamingScreenPayload(UUID id, String name, boolean global, boolean canSetGlobal) implements CustomPacketPayload {

    public static final Type<OpenNamingScreenPayload> TYPE = new Type<>(RPGRunestones.id("open_naming"));

    public static final StreamCodec<ByteBuf, OpenNamingScreenPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, OpenNamingScreenPayload::id,
            ByteBufCodecs.STRING_UTF8, OpenNamingScreenPayload::name,
            ByteBufCodecs.BOOL, OpenNamingScreenPayload::global,
            ByteBufCodecs.BOOL, OpenNamingScreenPayload::canSetGlobal,
            OpenNamingScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
