package net.veroxuniverse.verox_rpg_runestones.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneClientHooks;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ClientRunestones {

    private static final long AWAKEN_DURATION_MS = 2400L;
    private static final double ACTIVE_DISTANCE_SQR = 6.0 * 6.0;

    private static Set<UUID> known = Set.of();

    private ClientRunestones() {}

    public static void update(List<UUID> ids) {
        known = new HashSet<>(ids);
    }

    public static boolean knows(RunestoneBlockEntity blockEntity) {
        if (blockEntity.isGlobal()) return true;
        UUID id = blockEntity.getRunestoneId();
        return id != null && known.contains(id);
    }

    public static boolean isDormant(BlockGetter level, BlockPos lowerPos) {
        if (!(level instanceof ClientLevel)) return false;
        return level.getBlockEntity(lowerPos) instanceof RunestoneBlockEntity runestone && !knows(runestone);
    }

    public static RawAnimation selectAnimation(RunestoneBlockEntity blockEntity) {
        boolean isKnown = knows(blockEntity);
        Boolean lastKnown = blockEntity.getClientLastKnown();
        blockEntity.setClientLastKnown(isKnown);
        if (!isKnown) return RunestoneClientHooks.DORMANT;

        long now = Util.getMillis();
        if (Boolean.FALSE.equals(lastKnown)) {
            blockEntity.setClientAwakenUntil(now + AWAKEN_DURATION_MS);
        }
        if (now < blockEntity.getClientAwakenUntil()) return RunestoneClientHooks.AWAKEN;

        Player player = Minecraft.getInstance().player;
        if (player != null && player.distanceToSqr(Vec3.atBottomCenterOf(blockEntity.getBlockPos()).add(0.0, 1.0, 0.0)) <= ACTIVE_DISTANCE_SQR) {
            return RunestoneClientHooks.ACTIVE;
        }
        return RunestoneClientHooks.IDLE;
    }
}
