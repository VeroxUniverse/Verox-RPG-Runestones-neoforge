package net.veroxuniverse.verox_rpg_runestones.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.teleport.MenuSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record OpenRunestoneMenuPayload(
        MenuSource source,
        Optional<UUID> currentId,
        String currentName,
        int currentFlags,
        List<MenuEntry> entries,
        int costMode
) implements CustomPacketPayload {

    public static final int CAN_EDIT = 1;
    public static final int GLOBAL = 2;
    public static final int CAN_SET_GLOBAL = 4;

    public static final Type<OpenRunestoneMenuPayload> TYPE = new Type<>(RPGRunestones.id("open_menu"));

    public static final StreamCodec<ByteBuf, OpenRunestoneMenuPayload> STREAM_CODEC = StreamCodec.composite(
            MenuSource.STREAM_CODEC, OpenRunestoneMenuPayload::source,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), OpenRunestoneMenuPayload::currentId,
            ByteBufCodecs.STRING_UTF8, OpenRunestoneMenuPayload::currentName,
            ByteBufCodecs.VAR_INT, OpenRunestoneMenuPayload::currentFlags,
            MenuEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenRunestoneMenuPayload::entries,
            ByteBufCodecs.VAR_INT, OpenRunestoneMenuPayload::costMode,
            OpenRunestoneMenuPayload::new
    );

    public boolean canEdit() {
        return (this.currentFlags & CAN_EDIT) != 0;
    }

    public boolean global() {
        return (this.currentFlags & GLOBAL) != 0;
    }

    public boolean canSetGlobal() {
        return (this.currentFlags & CAN_SET_GLOBAL) != 0;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
