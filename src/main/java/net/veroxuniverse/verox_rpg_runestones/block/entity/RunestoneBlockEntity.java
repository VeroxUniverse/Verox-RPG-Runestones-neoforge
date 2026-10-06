package net.veroxuniverse.verox_rpg_runestones.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.verox_rpg_runestones.block.RunestoneClientHooks;
import net.veroxuniverse.verox_rpg_runestones.registry.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class RunestoneBlockEntity extends BlockEntity implements GeoBlockEntity {

    public static final String CONTROLLER = "main";
    public static final String TRIGGER_TELEPORT = "teleport";
    public static final String TRIGGER_ARRIVE = "arrive";
    private static final int TRANSITION_TICKS = 5;

    private static final String ID_KEY = "runestone_id";
    private static final String NAME_KEY = "name";
    private static final String OWNER_KEY = "owner_name";
    private static final String GLOBAL_KEY = "global";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID runestoneId;
    private String name = "";
    private String ownerName = "";
    private boolean global;

    private Boolean clientLastKnown;
    private long clientAwakenUntil;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNESTONE.get(), pos, state);
    }

    public UUID getRunestoneId() {
        return this.runestoneId;
    }

    public String getName() {
        return this.name;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public Boolean getClientLastKnown() {
        return this.clientLastKnown;
    }

    public void setClientLastKnown(boolean known) {
        this.clientLastKnown = known;
    }

    public long getClientAwakenUntil() {
        return this.clientAwakenUntil;
    }

    public void setClientAwakenUntil(long time) {
        this.clientAwakenUntil = time;
    }

    public void setRunestoneId(UUID id) {
        this.runestoneId = id;
        this.setChanged();
        this.syncToClients();
    }

    public void setDisplay(String name, String ownerName, boolean global) {
        this.name = name;
        this.ownerName = ownerName;
        this.global = global;
        this.setChanged();
        this.syncToClients();
    }

    private void syncToClients() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.writeShared(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.runestoneId = tag.hasUUID(ID_KEY) ? tag.getUUID(ID_KEY) : null;
        this.name = tag.getString(NAME_KEY);
        this.ownerName = tag.getString(OWNER_KEY);
        this.global = tag.getBoolean(GLOBAL_KEY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.writeShared(tag);
        return tag;
    }

    private void writeShared(CompoundTag tag) {
        if (this.runestoneId != null) {
            tag.putUUID(ID_KEY, this.runestoneId);
        }
        tag.putString(NAME_KEY, this.name);
        tag.putString(OWNER_KEY, this.ownerName);
        tag.putBoolean(GLOBAL_KEY, this.global);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, TRANSITION_TICKS, this::animate)
                .triggerableAnim(TRIGGER_TELEPORT, RunestoneClientHooks.TELEPORT)
                .triggerableAnim(TRIGGER_ARRIVE, RunestoneClientHooks.ARRIVE));
    }

    private PlayState animate(AnimationState<RunestoneBlockEntity> state) {
        return state.setAndContinue(RunestoneClientHooks.animationSelector.apply(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
