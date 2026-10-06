package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record MenuEntry(UUID id, String name, String dimension, BlockPos pos, int cost, int flags) {

    public static final int AFFORDABLE = 1;
    public static final int SOUL_ANCHOR = 2;
    public static final int GLOBAL = 4;

    public static final StreamCodec<ByteBuf, MenuEntry> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MenuEntry::id,
            ByteBufCodecs.STRING_UTF8, MenuEntry::name,
            ByteBufCodecs.STRING_UTF8, MenuEntry::dimension,
            BlockPos.STREAM_CODEC, MenuEntry::pos,
            ByteBufCodecs.VAR_INT, MenuEntry::cost,
            ByteBufCodecs.VAR_INT, MenuEntry::flags,
            MenuEntry::new
    );

    public boolean affordable() {
        return (this.flags & AFFORDABLE) != 0;
    }

    public boolean soulAnchor() {
        return (this.flags & SOUL_ANCHOR) != 0;
    }

    public boolean global() {
        return (this.flags & GLOBAL) != 0;
    }
}