package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.Optional;
import java.util.UUID;

public record ReorderRunestonesPayload(UUID moved, Optional<UUID> swapWith) implements CustomPacketPayload {

    public static final Type<ReorderRunestonesPayload> TYPE = new Type<>(RPGRunestones.id("reorder"));

    public static final StreamCodec<ByteBuf, ReorderRunestonesPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ReorderRunestonesPayload::moved,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), ReorderRunestonesPayload::swapWith,
            ReorderRunestonesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
