package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.List;
import java.util.UUID;

public record KnownRunestonesPayload(List<UUID> known) implements CustomPacketPayload {

    public static final Type<KnownRunestonesPayload> TYPE = new Type<>(RPGRunestones.id("known"));

    public static final StreamCodec<ByteBuf, KnownRunestonesPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), KnownRunestonesPayload::known,
            KnownRunestonesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
