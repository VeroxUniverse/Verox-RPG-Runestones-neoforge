package net.veroxuniverse.verox_rpg_runestones.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.veroxuniverse.verox_rpg_runestones.config.RunestonesConfig;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneEntry;
import net.veroxuniverse.verox_rpg_runestones.registry.ModItems;

public final class TeleportCost {

    private TeleportCost() {}

    public static RunestonesConfig.CostMode mode() {
        return RunestonesConfig.COST_MODE.get();
    }

    public static int calculate(ServerPlayer player, RunestoneEntry target, MenuSource source) {
        return calculate(player, target.dimension(), target.pos(), source);
    }

    public static int calculate(ServerPlayer player, ResourceKey<Level> dimension, BlockPos pos, MenuSource source) {
        if (player.isCreative() || mode() == RunestonesConfig.CostMode.NONE) return 0;

        int cost = source == MenuSource.TABLET ? RunestonesConfig.TABLET_BASE_COST.get() : RunestonesConfig.BASE_COST.get();
        if (!player.level().dimension().equals(dimension)) {
            return cost + RunestonesConfig.CROSS_DIMENSION_EXTRA_COST.get();
        }

        int blocksPerExtra = RunestonesConfig.BLOCKS_PER_EXTRA_COST.get();
        if (blocksPerExtra > 0) {
            double distance = Math.sqrt(player.blockPosition().distSqr(pos));
            cost += (int) (distance / blocksPerExtra);
        }
        return cost;
    }

    public static boolean canAfford(ServerPlayer player, int cost) {
        if (cost <= 0) return true;
        return switch (mode()) {
            case XP -> player.experienceLevel >= cost;
            case DUST -> countDust(player.getInventory()) >= cost;
            case NONE -> true;
        };
    }

    public static void pay(ServerPlayer player, int cost) {
        if (cost <= 0) return;
        switch (mode()) {
            case XP -> player.giveExperienceLevels(-cost);
            case DUST -> removeDust(player.getInventory(), cost);
            case NONE -> {
            }
        }
    }

    private static int countDust(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.RUNE_DUST.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeDust(Inventory inventory, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(ModItems.RUNE_DUST.get())) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        inventory.setChanged();
    }
}
