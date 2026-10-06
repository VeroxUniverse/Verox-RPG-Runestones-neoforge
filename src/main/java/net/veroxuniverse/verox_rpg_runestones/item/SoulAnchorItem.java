package net.veroxuniverse.verox_rpg_runestones.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.List;

public class SoulAnchorItem extends Item {

    public SoulAnchorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip." + RPGRunestones.MOD_ID + ".soul_anchor").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip." + RPGRunestones.MOD_ID + ".soul_anchor.slot").withStyle(ChatFormatting.DARK_GRAY));
    }
}
