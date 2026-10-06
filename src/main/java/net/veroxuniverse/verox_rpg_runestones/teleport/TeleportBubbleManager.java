package net.veroxuniverse.verox_rpg_runestones.teleport;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import net.veroxuniverse.verox_rpg_runestones.compat.CuriosCompat;
import net.veroxuniverse.verox_rpg_runestones.config.RunestonesConfig;
import net.veroxuniverse.verox_rpg_runestones.friends.FriendsData;
import net.veroxuniverse.verox_rpg_runestones.registry.ModItems;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class TeleportBubbleManager {

    private static final int SHELL_POINTS = 320;
    private static final int POINTS_PER_TICK = 90;
    private static final int FRONT_POINTS = 40;
    private static final double FRONT_WIDTH = 0.08;
    private static final int GLOW_EXTRA_TICKS = 10;
    private static final double BUBBLE_RADIUS = 4.0;
    private static final DustParticleOptions SHELL = new DustParticleOptions(new Vector3f(0.48F, 0.94F, 1.0F), 0.9F);
    private static final DustParticleOptions SHELL_LIGHT = new DustParticleOptions(new Vector3f(0.78F, 1.0F, 1.0F), 0.7F);
    private static final Vec3[] SPHERE = fibonacciSphere(SHELL_POINTS);

    private static final Map<UUID, Bubble> ACTIVE = new HashMap<>();

    private static final class Bubble {
        private final UUID traveler;
        private final ServerLevel origin;
        private final Vec3 center;
        private final ServerLevel targetLevel;
        private final Vec3 targetPos;
        private final float yaw;
        private final int cost;
        private final MenuSource source;
        private final UUID anchorFriend;
        private final BlockPos originStone;
        private final BlockPos destinationStone;
        private final int chargeTicks;
        private final double radius;
        private int ticks;

        private Bubble(ServerPlayer player, Vec3 center, BlockPos originStone, ServerLevel targetLevel, Vec3 targetPos, float yaw, int cost,
                       MenuSource source, UUID anchorFriend, BlockPos destinationStone) {
            this.originStone = originStone;
            this.destinationStone = destinationStone;
            this.traveler = player.getUUID();
            this.origin = player.serverLevel();
            this.center = center;
            this.targetLevel = targetLevel;
            this.targetPos = targetPos;
            this.yaw = yaw;
            this.cost = cost;
            this.source = source;
            this.anchorFriend = anchorFriend;
            this.chargeTicks = RunestonesConfig.BUBBLE_CHARGE_TICKS.get();
            this.radius = BUBBLE_RADIUS;
        }
    }

    private TeleportBubbleManager() {}

    public static boolean isCharging(UUID player) {
        return ACTIVE.containsKey(player);
    }

    private record PendingArrival(ServerLevel level, BlockPos pos, int ticks) {}

    private static final List<PendingArrival> PENDING_ARRIVALS = new ArrayList<>();
    private static final int ARRIVE_DELAY_TICKS = 4;

    public static void scheduleArrive(ServerLevel level, BlockPos pos) {
        if (pos != null) {
            PENDING_ARRIVALS.add(new PendingArrival(level, pos, ARRIVE_DELAY_TICKS));
        }
    }

    public static void start(ServerPlayer player, Vec3 center, BlockPos originStone, ServerLevel targetLevel, Vec3 targetPos, float yaw, int cost,
                             MenuSource source, UUID anchorFriend, BlockPos destinationStone) {
        if (isCharging(player.getUUID())) {
            RunestoneService.message(player, "teleport_busy");
            return;
        }

        Bubble bubble = new Bubble(player, center, originStone, targetLevel, targetPos, yaw, cost, source, anchorFriend, destinationStone);
        ACTIVE.put(player.getUUID(), bubble);
        RunestoneService.triggerAnimation(bubble.origin, originStone, RunestoneBlockEntity.TRIGGER_TELEPORT);

        bubble.origin.playSound(null, bubble.center.x, bubble.center.y, bubble.center.z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
        player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".teleport_charging").withStyle(ChatFormatting.AQUA), true);

        int glowTicks = bubble.chargeTicks + GLOW_EXTRA_TICKS;
        for (Entity passenger : passengers(player, bubble)) {
            if (passenger instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowTicks, 0, false, false));
            }
        }

        if (bubble.chargeTicks <= 0) {
            ACTIVE.remove(player.getUUID());
            complete(player, bubble);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickArrivals();
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = event.getServer();

        for (Bubble bubble : new ArrayList<>(ACTIVE.values())) {
            ServerPlayer player = server.getPlayerList().getPlayer(bubble.traveler);
            if (player == null || !player.isAlive() || player.serverLevel() != bubble.origin
                    || player.position().distanceToSqr(bubble.center) > bubble.radius * bubble.radius) {
                ACTIVE.remove(bubble.traveler);
                if (player != null) {
                    RunestoneService.message(player, "teleport_cancelled");
                }
                continue;
            }

            bubble.ticks++;
            spawnChargeParticles(bubble);
            if (bubble.ticks >= bubble.chargeTicks) {
                ACTIVE.remove(bubble.traveler);
                complete(player, bubble);
            }
        }
    }

    private static void tickArrivals() {
        if (PENDING_ARRIVALS.isEmpty()) return;
        List<PendingArrival> remaining = new ArrayList<>();
        for (PendingArrival arrival : PENDING_ARRIVALS) {
            if (arrival.ticks() <= 0) {
                RunestoneService.triggerAnimation(arrival.level(), arrival.pos(), RunestoneBlockEntity.TRIGGER_ARRIVE);
            } else {
                remaining.add(new PendingArrival(arrival.level(), arrival.pos(), arrival.ticks() - 1));
            }
        }
        PENDING_ARRIVALS.clear();
        PENDING_ARRIVALS.addAll(remaining);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE.clear();
        PENDING_ARRIVALS.clear();
    }

    private static void spawnChargeParticles(Bubble bubble) {
        float progress = bubble.chargeTicks <= 0 ? 1.0F : Mth.clamp(bubble.ticks / (float) bubble.chargeTicks, 0.0F, 1.0F);
        double front = progress * (1.0 + FRONT_WIDTH);
        RandomSource random = bubble.origin.getRandom();

        for (int i = 0; i < POINTS_PER_TICK; i++) {
            Vec3 point = SPHERE[random.nextInt(SPHERE.length)];
            double height = (point.y + 1.0) / 2.0;
            if (height > front) continue;
            emit(bubble, point, SHELL);
        }

        if (progress < 1.0F) {
            double frontY = Mth.clamp(front * 2.0 - 1.0, -1.0, 1.0);
            double ring = Math.sqrt(Math.max(0.0, 1.0 - frontY * frontY));
            double spin = bubble.ticks * 0.15;
            for (int i = 0; i < FRONT_POINTS; i++) {
                double angle = i * Math.PI * 2.0 / FRONT_POINTS + spin;
                emit(bubble, new Vec3(Math.cos(angle) * ring, frontY, Math.sin(angle) * ring), SHELL_LIGHT);
            }
        }

        if (bubble.ticks % 4 == 0) {
            bubble.origin.sendParticles(ParticleTypes.ENCHANT, bubble.center.x, bubble.center.y, bubble.center.z,
                    10, bubble.radius * 0.4, 0.8, bubble.radius * 0.4, 0.5);
        }
    }

    private static void emit(Bubble bubble, Vec3 unit, DustParticleOptions particle) {
        bubble.origin.sendParticles(particle,
                bubble.center.x + unit.x * bubble.radius,
                bubble.center.y + unit.y * bubble.radius,
                bubble.center.z + unit.z * bubble.radius,
                1, 0.0, 0.0, 0.0, 0.0);
    }

    private static Vec3[] fibonacciSphere(int count) {
        Vec3[] points = new Vec3[count];
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < count; i++) {
            double y = 1.0 - (i / (double) (count - 1)) * 2.0;
            double ring = Math.sqrt(1.0 - y * y);
            double angle = golden * i;
            points[i] = new Vec3(Math.cos(angle) * ring, y, Math.sin(angle) * ring);
        }
        return points;
    }

    private static void complete(ServerPlayer player, Bubble bubble) {
        ServerPlayer friend = null;
        if (bubble.anchorFriend != null) {
            friend = player.server.getPlayerList().getPlayer(bubble.anchorFriend);
            boolean valid = friend != null
                    && FriendsData.get(player.server).areFriends(player.getUUID(), friend.getUUID())
                    && CuriosCompat.isEquipped(friend, ModItems.SOUL_ANCHOR.get());
            if (!valid) {
                RunestoneService.message(player, "anchor_gone");
                return;
            }
        }

        if (!RunestoneService.canPay(player, bubble.cost)) return;
        TeleportCost.pay(player, bubble.cost);

        Vec3 destination = friend != null ? friend.position() : bubble.targetPos;
        ServerLevel destinationLevel = friend != null ? friend.serverLevel() : bubble.targetLevel;
        float yaw = friend != null ? friend.getYRot() : bubble.yaw;

        List<Entity> passengers = passengers(player, bubble);
        flash(bubble.origin, bubble.center);

        player.teleportTo(destinationLevel, destination.x, destination.y, destination.z, yaw, player.getXRot());
        for (Entity passenger : passengers) {
            Vec3 offset = passenger.position().subtract(bubble.center);
            move(passenger, destinationLevel, companionPosition(destinationLevel, destination, offset, bubble.radius), yaw);
        }

        flash(destinationLevel, destination);
        scheduleArrive(destinationLevel, bubble.destinationStone);
        RunestoneService.startTabletCooldown(player, bubble.source);

        if (!passengers.isEmpty()) {
            player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".companions", passengers.size())
                    .withStyle(ChatFormatting.AQUA), true);
        }
        if (friend != null) {
            friend.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".anchor_arrival",
                    player.getGameProfile().getName()).withStyle(ChatFormatting.AQUA), true);
        }
    }

    private static List<Entity> passengers(ServerPlayer player, Bubble bubble) {
        double radius = bubble.radius;
        AABB area = AABB.ofSize(bubble.center, radius * 2.0, radius * 2.0, radius * 2.0);
        List<Entity> result = new ArrayList<>();
        for (Entity entity : bubble.origin.getEntities(player, area, candidate -> candidate.distanceToSqr(bubble.center) <= radius * radius)) {
            if (entity.isPassenger()) continue;
            if (travels(player, entity)) result.add(entity);
        }
        return result;
    }

    private static boolean travels(ServerPlayer traveler, Entity entity) {
        if (entity instanceof ServerPlayer other) {
            return RunestonesConfig.BUBBLE_TAKES_FRIENDS.get()
                    && !other.isSpectator()
                    && !isCharging(other.getUUID())
                    && FriendsData.get(traveler.server).areFriends(traveler.getUUID(), other.getUUID());
        }
        if (!RunestonesConfig.BUBBLE_TAKES_CREATURES.get()) return false;
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        return !(entity instanceof Enemy)
                && !(entity instanceof ArmorStand)
                && !entity.getType().is(Tags.EntityTypes.BOSSES);
    }

    private static Vec3 companionPosition(ServerLevel level, Vec3 destination, Vec3 offset, double radius) {
        double horizontal = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        double scale = horizontal > radius ? radius / horizontal : 1.0;
        Vec3 candidate = destination.add(offset.x * scale, 0.0, offset.z * scale);
        BlockPos base = BlockPos.containing(candidate);
        for (int dy : new int[]{0, 1, -1}) {
            BlockPos pos = base.above(dy);
            if (RunestoneService.isStandable(level, pos)) {
                return new Vec3(candidate.x, pos.getY(), candidate.z);
            }
        }
        return destination;
    }

    private static void move(Entity entity, ServerLevel level, Vec3 pos, float yaw) {
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, pos.x, pos.y, pos.z, yaw, player.getXRot());
        } else if (entity.level() == level) {
            entity.teleportTo(pos.x, pos.y, pos.z);
            entity.setYRot(yaw);
        } else {
            entity.changeDimension(new DimensionTransition(level, pos, Vec3.ZERO, yaw, entity.getXRot(), DimensionTransition.DO_NOTHING));
        }
    }

    private static void flash(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 1.0, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        RunestoneService.playEffects(level, pos);
    }
}