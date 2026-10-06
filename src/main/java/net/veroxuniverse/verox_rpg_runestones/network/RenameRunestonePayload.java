package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.UUID;

public record RenameRunestonePayload(UUID id, String name, boolean global, boolean reopenMenu) implements CustomPacketPayload {

    public static final Type<RenameRunestonePayload> TYPE = new Type<>(RPGRunestones.id("rename"));

    public static final StreamCodec<ByteBuf, RenameRunestonePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RenameRunestonePayload::id,
            ByteBufCodecs.STRING_UTF8, RenameRunestonePayload::name,
            ByteBufCodecs.BOOL, RenameRunestonePayload::global,
            ByteBufCodecs.BOOL, RenameRunestonePayload::reopenMenu,
            RenameRunestonePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
