package net.veroxuniverse.verox_rpg_runestones.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneBlock;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RunestoneComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = RPGRunestones.id("runestone");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockPos pos = accessor.getPosition();
        if (accessor.getBlockState().getValue(RunestoneBlock.HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }
        if (!(accessor.getLevel().getBlockEntity(pos) instanceof RunestoneBlockEntity runestone)) return;
        if (runestone.getName().isEmpty()) return;

        tooltip.add(Component.literal(runestone.getName()).withStyle(runestone.isGlobal() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA));
        if (runestone.isGlobal()) {
            tooltip.add(Component.translatable("jade." + RPGRunestones.MOD_ID + ".global").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (!runestone.getOwnerName().isEmpty()) {
            tooltip.add(Component.translatable("jade." + RPGRunestones.MOD_ID + ".owner", runestone.getOwnerName()).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}