/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.entity;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.config.BCConfig;
import buildcraft.registry.BCItems;
import buildcraft.robotics.tile.TileRobotStation;

/**
 * Flying robot with selectable behaviours. Boards set the mode: red = haul items to a station,
 * green = follow the owner, blue = guard against hostiles. Default is pickup-and-wander.
 */
public class RobotEntity extends PathfinderMob {

    public enum Mode {
        PICKUP, HAUL, FOLLOW, GUARD
    }

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(RobotEntity.class, EntityDataSerializers.INT);

    private final ItemStackHandler inventory = new ItemStackHandler(9);
    @Nullable
    private UUID ownerId;

    public RobotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_MODE, Mode.PICKUP.ordinal());
    }

    public Mode getMode() {
        int idx = entityData.get(DATA_MODE);
        Mode[] values = Mode.values();
        return idx >= 0 && idx < values.length ? values[idx] : Mode.PICKUP;
    }

    public void setMode(Mode mode) {
        entityData.set(DATA_MODE, mode.ordinal());
    }

    public void setOwner(@Nullable UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Nullable
    public Player getOwner() {
        if (ownerId == null || !(level() instanceof ServerLevel server)) {
            return null;
        }
        Player player = server.getServer().getPlayerList().getPlayer(ownerId);
        if (player == null || player.level() != level()) {
            return null;
        }
        return player;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public boolean isInventoryFull() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            if (slot.isEmpty() || slot.getCount() < slot.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public boolean canInsert(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < inventory.getSlots() && !remaining.isEmpty(); i++) {
            remaining = inventory.insertItem(i, remaining, true);
        }
        return remaining.getCount() < stack.getCount();
    }

    public boolean isInventoryEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RobotCollectItemsGoal(this));
        goalSelector.addGoal(2, new RobotReturnToStationGoal(this));
        goalSelector.addGoal(3, new RobotFollowOwnerGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, true));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                living -> getMode() == Mode.GUARD));
        goalSelector.addGoal(8, new WaterAvoidingRandomFlyingGoal(this, 1.0));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        Mode next = null;
        if (held.is(BCItems.BOARD_RED.get())) {
            next = Mode.HAUL;
        } else if (held.is(BCItems.BOARD_GREEN.get())) {
            next = Mode.FOLLOW;
        } else if (held.is(BCItems.BOARD_BLUE.get())) {
            next = Mode.GUARD;
        } else if (held.is(BCItems.BOARD_BLANK.get())) {
            next = Mode.PICKUP;
        }
        if (next != null) {
            if (!level().isClientSide) {
                setOwner(player.getUUID());
                setMode(next);
                player.displayClientMessage(Component.translatable("message.buildcraft.robot.mode",
                        Component.translatable("robot.mode.buildcraft." + next.name().toLowerCase())), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public ItemStack insert(ItemStack stack) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            stack = inventory.insertItem(slot, stack, false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting,
            boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(new ItemStack(BCItems.ROBOT.get()));
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack.copy());
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("mode", getMode().ordinal());
        if (ownerId != null) {
            tag.putUUID("owner", ownerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        if (tag.contains("mode")) {
            entityData.set(DATA_MODE, tag.getInt("mode"));
        }
        if (tag.hasUUID("owner")) {
            ownerId = tag.getUUID("owner");
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean isAffectedByFluids() {
        return false;
    }

    /** Vacuum nearby drops into the internal inventory. */
    void collectNearbyItems() {
        double range = BCConfig.robotPickupRange();
        List<ItemEntity> nearby = level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(range),
                e -> e.isAlive() && !e.getItem().isEmpty() && !e.hasPickUpDelay() && canInsert(e.getItem()));
        ItemEntity target = null;
        double best = Double.MAX_VALUE;
        for (ItemEntity entity : nearby) {
            double d = distanceToSqr(entity);
            if (d < best) {
                best = d;
                target = entity;
            }
        }
        if (target == null) {
            return;
        }
        if (best < 2.0) {
            ItemStack leftover = insert(target.getItem().copy());
            if (leftover.isEmpty()) {
                target.discard();
            } else {
                target.setItem(leftover);
            }
        } else {
            getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2);
        }
    }

    boolean depositInto(TileRobotStation station) {
        var cap = station.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, null)
                .orElse(null);
        if (cap == null) {
            return false;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = ItemHandlerHelper.insertItem(cap, stack, false);
            if (leftover.getCount() != stack.getCount()) {
                inventory.setStackInSlot(i, leftover);
                return true;
            }
        }
        return false;
    }

    @Nullable
    TileRobotStation findNearestStation() {
        if (!(level() instanceof ServerLevel server)) {
            return null;
        }
        int range = BCConfig.robotStationRange();
        int chunkRange = (range >> 4) + 1;
        int cx = blockPosition().getX() >> 4;
        int cz = blockPosition().getZ() >> 4;
        TileRobotStation closest = null;
        double best = (double) range * range;
        for (int dx = -chunkRange; dx <= chunkRange; dx++) {
            for (int dz = -chunkRange; dz <= chunkRange; dz++) {
                var chunk = server.getChunkSource().getChunkNow(cx + dx, cz + dz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof TileRobotStation station && !station.isRemoved()) {
                        double d = distanceToSqr(be.getBlockPos().getX() + 0.5, be.getBlockPos().getY() + 0.5,
                                be.getBlockPos().getZ() + 0.5);
                        if (d < best) {
                            best = d;
                            closest = station;
                        }
                    }
                }
            }
        }
        return closest;
    }
}
