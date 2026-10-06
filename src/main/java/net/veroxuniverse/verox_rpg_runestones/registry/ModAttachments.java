package net.veroxuniverse.verox_rpg_runestones.registry;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.data.PlayerRunestones;

import java.util.function.Supplier;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RPGRunestones.MOD_ID);

    public static final Supplier<AttachmentType<PlayerRunestones>> PLAYER_RUNESTONES = ATTACHMENT_TYPES.register("player_runestones",
            () -> AttachmentType.builder(() -> PlayerRunestones.EMPTY)
                    .serialize(PlayerRunestones.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {}
}
