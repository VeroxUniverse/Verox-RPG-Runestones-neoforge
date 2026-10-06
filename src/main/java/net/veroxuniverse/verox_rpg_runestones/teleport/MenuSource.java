package net.veroxuniverse.verox_rpg_runestones.teleport;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum MenuSource {
    RUNESTONE,
    TABLET;

    public static final StreamCodec<ByteBuf, MenuSource> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(index -> values()[index], MenuSource::ordinal);
}
