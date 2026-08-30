/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.entity;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A simple flying "picker" robot: wanders the area and vacuums up nearby dropped items into its
 * internal inventory, dropping everything when destroyed. A functional stand-in for BuildCraft's
 * fully programmable robots.
 */
public class RobotEntity extends PathfinderMob {

    private final ItemStackHandler inventory = new ItemStackHandler(9);

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
                .add(Attributes.FOLLOW_RANGE, 24.0);
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
        goalSelector.addGoal(1, new WaterAvoidingRandomFlyingGoal(this, 1.0));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            collectItems();
        }
    }

    private void collectItems() {
        List<ItemEntity> nearby = level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(6.0),
                e -> e.isAlive() && !e.getItem().isEmpty());
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

    private ItemStack insert(ItemStack stack) {
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
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("inventory", inventory.serializeNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean isAffectedByFluids() {
        return false;
    }
}
