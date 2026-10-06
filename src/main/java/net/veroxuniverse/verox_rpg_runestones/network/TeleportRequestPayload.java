package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.teleport.MenuSource;

import java.util.Optional;
import java.util.UUID;

public record TeleportRequestPayload(UUID target, MenuSource source, Optional<UUID> from) implements CustomPacketPayload {

    public static final Type<TeleportRequestPayload> TYPE = new Type<>(RPGRunestones.id("teleport"));

    public static final StreamCodec<ByteBuf, TeleportRequestPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TeleportRequestPayload::target,
            MenuSource.STREAM_CODEC, TeleportRequestPayload::source,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), TeleportRequestPayload::from,
            TeleportRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
