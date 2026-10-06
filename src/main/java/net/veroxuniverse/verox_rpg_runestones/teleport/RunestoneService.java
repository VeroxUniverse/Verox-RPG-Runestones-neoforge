package net.veroxuniverse.verox_rpg_runestones.teleport;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneBlock;
import net.veroxuniverse.verox_rpg_runestones.block.entity.RunestoneBlockEntity;
import net.veroxuniverse.verox_rpg_runestones.compat.CuriosCompat;
import net.veroxuniverse.verox_rpg_runestones.config.RunestonesConfig;
import net.veroxuniverse.verox_rpg_runestones.data.PlayerRunestones;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneNames;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneEntry;
import net.veroxuniverse.verox_rpg_runestones.data.RunestoneRegistry;
import net.veroxuniverse.verox_rpg_runestones.friends.FriendsData;
import net.veroxuniverse.verox_rpg_runestones.network.KnownRunestonesPayload;
import net.veroxuniverse.verox_rpg_runestones.network.MenuEntry;
import net.veroxuniverse.verox_rpg_runestones.network.OpenNamingScreenPayload;
import net.veroxuniverse.verox_rpg_runestones.network.OpenRunestoneMenuPayload;
import net.veroxuniverse.verox_rpg_runestones.network.RenameRunestonePayload;
import net.veroxuniverse.verox_rpg_runestones.network.ReorderRunestonesPayload;
import net.veroxuniverse.verox_rpg_runestones.network.TeleportRequestPayload;
import net.veroxuniverse.verox_rpg_runestones.registry.ModAttachments;
import net.veroxuniverse.verox_rpg_runestones.registry.ModItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = RPGRunestones.MOD_ID)
public final class RunestoneService {

    public static final int MAX_NAME_LENGTH = 20;
    private static final double MAX_USE_DISTANCE_SQR = 10.0 * 10.0;

    private RunestoneService() {}

    public static void onPlaced(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack stack) {
        RunestoneRegistry registry = RunestoneRegistry.get(level.getServer());
        boolean named = stack.has(DataComponents.CUSTOM_NAME);
        String name = named ? clampName(stack.getHoverName().getString()) : defaultName(level, pos);

        RunestoneEntry entry = new RunestoneEntry(UUID.randomUUID(), name, level.dimension(), pos.immutable(), player.getUUID(), false);
        registry.add(entry);
        bindBlockEntity(level, pos, entry, player.getGameProfile().getName());
        discover(player, entry, false);

        if (!named) {
            PacketDistributor.sendToPlayer(player, new OpenNamingScreenPayload(entry.id(), entry.name(), false, canSetGlobal(player)));
        }
    }

    public static void onRemoved(ServerLevel level, BlockPos pos) {
        RunestoneRegistry registry = RunestoneRegistry.get(level.getServer());
        registry.findAt(level.dimension(), pos).ifPresent(entry -> registry.remove(entry.id()));
    }

    public static void onUse(ServerLevel level, BlockPos lower, ServerPlayer player) {
        RunestoneRegistry registry = RunestoneRegistry.get(level.getServer());
        RunestoneEntry entry = registry.findAt(level.dimension(), lower).orElseGet(() -> {
            RunestoneEntry created = new RunestoneEntry(UUID.randomUUID(), defaultName(level, lower), level.dimension(), lower.immutable(), Util.NIL_UUID, false);
            registry.add(created);
            bindBlockEntity(level, lower, created, "");
            return created;
        });

        discover(player, entry, true);
        openMenu(player, MenuSource.RUNESTONE, entry);
    }

    public static void openFromTablet(ServerPlayer player) {
        long now = player.server.overworld().getGameTime();
        PlayerRunestones data = player.getData(ModAttachments.PLAYER_RUNESTONES.get());
        if (now < data.tabletReadyAt()) {
            long seconds = (data.tabletReadyAt() - now + 19) / 20;
            player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".tablet_cooldown",
                    formatTime(seconds)).withStyle(ChatFormatting.RED), true);
            return;
        }
        openMenu(player, MenuSource.TABLET, null);
    }

    public static void openMenu(ServerPlayer player, MenuSource source, RunestoneEntry current) {
        RunestoneRegistry registry = RunestoneRegistry.get(player.server);
        PlayerRunestones data = syncGlobalRunestones(player, registry);
        boolean crossDimension = RunestonesConfig.ALLOW_CROSS_DIMENSION.get();

        List<MenuEntry> entries = new ArrayList<>();
        for (ServerPlayer friend : anchoredFriends(player)) {
            if (!crossDimension && !friend.level().dimension().equals(player.level().dimension())) continue;
            int cost = TeleportCost.calculate(player, friend.level().dimension(), friend.blockPosition(), source);
            int flags = MenuEntry.SOUL_ANCHOR | (TeleportCost.canAfford(player, cost) ? MenuEntry.AFFORDABLE : 0);
            entries.add(new MenuEntry(friend.getUUID(), friend.getGameProfile().getName(),
                    friend.level().dimension().location().toString(), friend.blockPosition(), cost, flags));
        }
        for (UUID id : data.known()) {
            Optional<RunestoneEntry> known = registry.get(id);
            if (known.isEmpty()) continue;

            RunestoneEntry target = known.get();
            if (current != null && target.id().equals(current.id())) continue;
            if (!crossDimension && !target.dimension().equals(player.level().dimension())) continue;

            int cost = TeleportCost.calculate(player, target, source);
            entries.add(new MenuEntry(target.id(), target.name(), target.dimension().location().toString(),
                    target.pos(), cost, (TeleportCost.canAfford(player, cost) ? MenuEntry.AFFORDABLE : 0) | (target.global() ? MenuEntry.GLOBAL : 0)));
        }

        PacketDistributor.sendToPlayer(player, new OpenRunestoneMenuPayload(
                source,
                current == null ? Optional.empty() : Optional.of(current.id()),
                current == null ? "" : current.name(),
                currentFlags(player, current),
                entries,
                TeleportCost.mode().ordinal()));
    }

    public static void handleTeleport(ServerPlayer player, TeleportRequestPayload payload) {
        RunestoneRegistry registry = RunestoneRegistry.get(player.server);
        PlayerRunestones data = player.getData(ModAttachments.PLAYER_RUNESTONES.get());
        long now = player.server.overworld().getGameTime();

        if (!validSource(player, payload, registry, data, now)) return;

        Optional<RunestoneEntry> found = registry.get(payload.target());
        if (found.isEmpty()) {
            teleportToAnchor(player, payload);
            return;
        }

        RunestoneEntry target = found.get();
        if (!data.knows(target.id())) return;
        if (payload.from().isPresent() && payload.from().get().equals(target.id())) return;
        if (!dimensionAllowed(player, target.dimension())) return;

        ServerLevel targetLevel = player.server.getLevel(target.dimension());
        if (targetLevel == null || !RunestoneBlock.isLowerRunestone(targetLevel, target.pos())) {
            registry.remove(target.id());
            message(player, "unknown_target");
            return;
        }

        int cost = TeleportCost.calculate(player, target, payload.source());
        if (!canPay(player, cost)) return;

        Destination destination = findDestination(targetLevel, target.pos());
        if (payload.source() == MenuSource.TABLET) {
            TeleportCost.pay(player, cost);
            travel(player, targetLevel, destination.pos(), destination.yaw());
            TeleportBubbleManager.scheduleArrive(targetLevel, target.pos());
            startTabletCooldown(player, payload.source());
            return;
        }
        TeleportBubbleManager.start(player, bubbleCenter(player, payload, registry), originStone(payload, registry), targetLevel,
                destination.pos(), destination.yaw(), cost, payload.source(), null, target.pos());
    }

    private static void teleportToAnchor(ServerPlayer player, TeleportRequestPayload payload) {
        ServerPlayer friend = player.server.getPlayerList().getPlayer(payload.target());
        boolean valid = friend != null
                && FriendsData.get(player.server).areFriends(player.getUUID(), friend.getUUID())
                && CuriosCompat.isEquipped(friend, ModItems.SOUL_ANCHOR.get());
        if (!valid) {
            message(player, "anchor_gone");
            return;
        }
        if (!dimensionAllowed(player, friend.level().dimension())) return;

        int cost = TeleportCost.calculate(player, friend.level().dimension(), friend.blockPosition(), payload.source());
        if (!canPay(player, cost)) return;

        if (payload.source() == MenuSource.TABLET) {
            TeleportCost.pay(player, cost);
            travel(player, friend.serverLevel(), friend.position(), friend.getYRot());
            friend.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".anchor_arrival",
                    player.getGameProfile().getName()).withStyle(ChatFormatting.AQUA), true);
            startTabletCooldown(player, payload.source());
            return;
        }

        RunestoneRegistry registry = RunestoneRegistry.get(player.server);
        TeleportBubbleManager.start(player, bubbleCenter(player, payload, registry), originStone(payload, registry), friend.serverLevel(),
                friend.position(), friend.getYRot(), cost, payload.source(), friend.getUUID(), null);
    }

    private static BlockPos originStone(TeleportRequestPayload payload, RunestoneRegistry registry) {
        if (payload.source() != MenuSource.RUNESTONE) return null;
        return payload.from().flatMap(registry::get).map(RunestoneEntry::pos).orElse(null);
    }

    static void triggerAnimation(ServerLevel level, BlockPos pos, String animation) {
        if (pos != null && level.isLoaded(pos) && level.getBlockEntity(pos) instanceof RunestoneBlockEntity runestone) {
            runestone.triggerAnim(RunestoneBlockEntity.CONTROLLER, animation);
        }
    }

    public static void syncKnown(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new KnownRunestonesPayload(player.getData(ModAttachments.PLAYER_RUNESTONES.get()).known()));
    }

    private static Vec3 bubbleCenter(ServerPlayer player, TeleportRequestPayload payload, RunestoneRegistry registry) {
        if (payload.source() == MenuSource.RUNESTONE) {
            Optional<RunestoneEntry> from = payload.from().flatMap(registry::get);
            if (from.isPresent()) {
                return Vec3.atBottomCenterOf(from.get().pos()).add(0.0, 1.0, 0.0);
            }
        }
        return player.position().add(0.0, 1.0, 0.0);
    }

    private static boolean validSource(ServerPlayer player, TeleportRequestPayload payload, RunestoneRegistry registry, PlayerRunestones data, long now) {
        if (payload.source() == MenuSource.RUNESTONE) {
            Optional<RunestoneEntry> from = payload.from().flatMap(registry::get);
            if (from.isEmpty() || !from.get().dimension().equals(player.level().dimension())
                    || player.blockPosition().distSqr(from.get().pos()) > MAX_USE_DISTANCE_SQR) {
                message(player, "too_far");
                return false;
            }
            return true;
        }
        boolean holding = player.getMainHandItem().is(ModItems.RUNE_TABLET.get()) || player.getOffhandItem().is(ModItems.RUNE_TABLET.get());
        return holding && now >= data.tabletReadyAt();
    }

    private static boolean dimensionAllowed(ServerPlayer player, ResourceKey<Level> dimension) {
        if (dimension.equals(player.level().dimension()) || RunestonesConfig.ALLOW_CROSS_DIMENSION.get()) return true;
        message(player, "no_dimension");
        return false;
    }

    static boolean canPay(ServerPlayer player, int cost) {
        if (TeleportCost.canAfford(player, cost)) return true;
        String key = TeleportCost.mode() == RunestonesConfig.CostMode.XP ? "not_enough_xp" : "not_enough_dust";
        player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + "." + key, cost)
                .withStyle(ChatFormatting.RED), true);
        return false;
    }

    static void startTabletCooldown(ServerPlayer player, MenuSource source) {
        if (source != MenuSource.TABLET) return;
        long now = player.server.overworld().getGameTime();
        int cooldown = RunestonesConfig.TABLET_COOLDOWN_SECONDS.get() * 20;
        if (cooldown <= 0) return;
        player.setData(ModAttachments.PLAYER_RUNESTONES.get(), player.getData(ModAttachments.PLAYER_RUNESTONES.get()).withTabletReadyAt(now + cooldown));
        player.getCooldowns().addCooldown(ModItems.RUNE_TABLET.get(), cooldown);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncKnown(player);
        long remaining = player.getData(ModAttachments.PLAYER_RUNESTONES.get()).tabletReadyAt() - player.server.overworld().getGameTime();
        if (remaining > 0) {
            player.getCooldowns().addCooldown(ModItems.RUNE_TABLET.get(), (int) Math.min(Integer.MAX_VALUE, remaining));
        }
    }

    private static void travel(ServerPlayer player, ServerLevel level, Vec3 pos, float yaw) {
        playEffects(player.serverLevel(), player.position());
        player.teleportTo(level, pos.x, pos.y, pos.z, yaw, player.getXRot());
        playEffects(level, pos);
    }

    private static List<ServerPlayer> anchoredFriends(ServerPlayer player) {
        List<ServerPlayer> result = new ArrayList<>();
        for (UUID id : FriendsData.get(player.server).friendsOf(player.getUUID())) {
            ServerPlayer friend = player.server.getPlayerList().getPlayer(id);
            if (friend != null && !friend.isSpectator() && CuriosCompat.isEquipped(friend, ModItems.SOUL_ANCHOR.get())) {
                result.add(friend);
            }
        }
        return result;
    }

    public static void handleReorder(ServerPlayer player, ReorderRunestonesPayload payload) {
        PlayerRunestones data = player.getData(ModAttachments.PLAYER_RUNESTONES.get());
        PlayerRunestones updated = payload.swapWith()
                .map(other -> data.withSwapped(payload.moved(), other))
                .orElseGet(() -> data.withMovedToEnd(payload.moved()));
        if (updated != data) {
            player.setData(ModAttachments.PLAYER_RUNESTONES.get(), updated);
            syncKnown(player);
        }
    }

    public static void handleRename(ServerPlayer player, RenameRunestonePayload payload) {
        String name = clampName(payload.name().trim());
        if (name.isEmpty()) return;

        RunestoneRegistry registry = RunestoneRegistry.get(player.server);
        Optional<RunestoneEntry> found = registry.get(payload.id());
        if (found.isEmpty() || !canRename(player, found.get())) return;

        registry.rename(payload.id(), name);
        if (canSetGlobal(player)) {
            registry.setGlobal(payload.id(), payload.global());
        }
        registry.get(payload.id()).ifPresent(entry -> refreshBlockEntity(player.server, entry));

        if (payload.reopenMenu()) {
            RunestoneEntry renamed = registry.get(payload.id()).orElseThrow();
            boolean near = renamed.dimension().equals(player.level().dimension())
                    && player.blockPosition().distSqr(renamed.pos()) <= MAX_USE_DISTANCE_SQR;
            if (near) {
                openMenu(player, MenuSource.RUNESTONE, renamed);
            }
        }
    }

    public static boolean canSetGlobal(ServerPlayer player) {
        return player.isCreative() || player.hasPermissions(2) || RunestonesConfig.PLAYERS_CAN_SET_GLOBAL.get();
    }

    private static int currentFlags(ServerPlayer player, RunestoneEntry current) {
        if (current == null) return 0;
        int flags = 0;
        boolean canEdit = canRename(player, current);
        if (canEdit) flags |= OpenRunestoneMenuPayload.CAN_EDIT;
        if (current.global()) flags |= OpenRunestoneMenuPayload.GLOBAL;
        if (canEdit && canSetGlobal(player)) flags |= OpenRunestoneMenuPayload.CAN_SET_GLOBAL;
        return flags;
    }

    private static PlayerRunestones syncGlobalRunestones(ServerPlayer player, RunestoneRegistry registry) {
        PlayerRunestones data = player.getData(ModAttachments.PLAYER_RUNESTONES.get());
        PlayerRunestones updated = data;
        for (UUID id : data.granted()) {
            Optional<RunestoneEntry> entry = registry.get(id);
            if (entry.isEmpty() || !entry.get().global()) {
                updated = updated.withoutGranted(id);
            }
        }
        for (RunestoneEntry entry : registry.all()) {
            if (entry.global()) {
                updated = updated.withGranted(entry.id());
            }
        }
        if (updated != data) {
            player.setData(ModAttachments.PLAYER_RUNESTONES.get(), updated);
            syncKnown(player);
        }
        return updated;
    }

    public static boolean canRename(ServerPlayer player, RunestoneEntry entry) {
        return entry.owner().equals(player.getUUID()) || player.isCreative() || player.hasPermissions(2);
    }

    private static void discover(ServerPlayer player, RunestoneEntry entry, boolean notify) {
        PlayerRunestones data = player.getData(ModAttachments.PLAYER_RUNESTONES.get());
        if (data.knows(entry.id())) return;

        player.setData(ModAttachments.PLAYER_RUNESTONES.get(), data.withKnown(entry.id()));
        syncKnown(player);
        if (notify) {
            player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + ".discovered", entry.name())
                    .withStyle(ChatFormatting.AQUA), true);
            player.playNotifySound(SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static void bindBlockEntity(ServerLevel level, BlockPos pos, RunestoneEntry entry, String ownerName) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RunestoneBlockEntity runestone) {
            runestone.setRunestoneId(entry.id());
            runestone.setDisplay(entry.name(), ownerName, entry.global());
        }
    }

    public static void refreshBlockEntity(MinecraftServer server, RunestoneEntry entry) {
        ServerLevel level = server.getLevel(entry.dimension());
        if (level == null || !level.isLoaded(entry.pos())) return;
        if (level.getBlockEntity(entry.pos()) instanceof RunestoneBlockEntity runestone) {
            String ownerName = entry.owner().equals(Util.NIL_UUID) ? "" : FriendsData.get(server).nameOf(entry.owner());
            runestone.setDisplay(entry.name(), ownerName, entry.global());
        }
    }

    public static void teleportDirect(ServerPlayer player, ServerLevel level, BlockPos stone) {
        Destination destination = findDestination(level, stone);
        travel(player, level, destination.pos(), destination.yaw());
    }

    public static boolean canBreak(ServerPlayer player, RunestoneEntry entry) {
        if (!RunestonesConfig.PROTECT_RUNESTONES.get()) return true;
        if (player.isCreative() || player.hasPermissions(2)) return true;
        boolean owner = entry.owner().equals(player.getUUID());
        if (entry.global()) return owner && RunestonesConfig.PLAYERS_CAN_SET_GLOBAL.get();
        return owner;
    }

    private record Destination(Vec3 pos, float yaw) {}

    private static Destination findDestination(ServerLevel level, BlockPos stone) {
        BlockState state = level.getBlockState(stone);
        Direction facing = state.hasProperty(RunestoneBlock.FACING) ? state.getValue(RunestoneBlock.FACING) : Direction.NORTH;
        Direction[] order = {facing, facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite()};

        for (Direction direction : order) {
            BlockPos side = stone.relative(direction);
            for (int dy : new int[]{0, 1, -1}) {
                BlockPos candidate = side.above(dy);
                if (isStandable(level, candidate)) {
                    return new Destination(Vec3.atBottomCenterOf(candidate), direction.getOpposite().toYRot());
                }
            }
        }
        return new Destination(Vec3.atBottomCenterOf(stone.above(2)), facing.toYRot());
    }

    static boolean isStandable(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getFluidState(pos).isEmpty();
    }

    static void playEffects(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1.0, pos.z, 40, 0.4, 0.8, 0.4, 0.2);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message." + RPGRunestones.MOD_ID + "." + key).withStyle(ChatFormatting.RED), true);
    }

    private static String defaultName(ServerLevel level, BlockPos pos) {
        Set<String> used = new HashSet<>();
        for (RunestoneEntry entry : RunestoneRegistry.get(level.getServer()).all()) {
            used.add(entry.name());
        }
        return clampName(RunestoneNames.pick(level.getRandom(), used));
    }

    private static String clampName(String name) {
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    private static String formatTime(long seconds) {
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }
}