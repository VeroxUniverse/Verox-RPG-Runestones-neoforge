package net.veroxuniverse.verox_rpg_runestones.teleport;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneBlock;
import net.veroxuniverse.verox_rpg_runestones.config.RunestonesConfig;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneEntry;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneRegistry;

import java.util.Optional;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class RunestoneProtection {

    private RunestoneProtection() {}

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof RunestoneBlock)) return;
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockPos lower = state.getValue(RunestoneBlock.HALF) == DoubleBlockHalf.UPPER ? event.getPos().below() : event.getPos();
        Optional<RunestoneEntry> entry = RunestoneRegistry.get(level.getServer()).findAt(level.dimension(), lower);
        if (entry.isPresent() && !RunestoneService.canBreak(player, entry.get())) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".protected").withStyle(ChatFormatting.RED), true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!RunestonesConfig.PROTECT_RUNESTONES.get()) return;
        event.getAffectedBlocks().removeIf(pos -> event.getLevel().getBlockState(pos).getBlock() instanceof RunestoneBlock);
    }
}
