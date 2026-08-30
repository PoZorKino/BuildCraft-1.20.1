/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Robot Station: a stationary "picker robot". When powered it vacuums up nearby dropped items into
 * its buffer and pushes them into an adjacent inventory. A functional stand-in for BuildCraft's
 * programmable robots (whose full AI is not part of this port).
 */
public class TileRobotStation extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 10_000;
    public static final int MAX_RECEIVE = 500;
    public static final int COST_PER_PICKUP = 20;
    public static final double RADIUS = 4.0;
    public static final int INTERVAL = 5;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final ItemStackHandler buffer = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> buffer);
    private int cooldown;

    public TileRobotStation(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ROBOT_STATION.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        pushToNeighbours(level, pos);

        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < COST_PER_PICKUP) {
            return;
        }
        AABB area = new AABB(pos).inflate(RADIUS);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area,
                e -> e.isAlive() && !e.getItem().isEmpty() && !e.hasPickUpDelay());
        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            ItemStack leftover = insert(stack.copy());
            if (leftover.getCount() != stack.getCount()) {
                if (leftover.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(leftover);
                }
                energy.spend(COST_PER_PICKUP);
                cooldown = INTERVAL;
                setChanged();
                return;
            }
        }
    }

    private ItemStack insert(ItemStack stack) {
        for (int slot = 0; slot < buffer.getSlots(); slot++) {
            stack = buffer.insertItem(slot, stack, false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    private void pushToNeighbours(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof TileRobotStation) {
                continue;
            }
            IItemHandler dest = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            if (dest == null) {
                continue;
            }
            for (int slot = 0; slot < buffer.getSlots(); slot++) {
                ItemStack in = buffer.getStackInSlot(slot);
                if (!in.isEmpty()) {
                    ItemStack leftover = ItemHandlerHelper.insertItem(dest, in, false);
                    if (leftover.getCount() != in.getCount()) {
                        buffer.setStackInSlot(slot, leftover);
                        setChanged();
                    }
                }
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer c = new SimpleContainer(buffer.getSlots());
        for (int i = 0; i < buffer.getSlots(); i++) {
            c.setItem(i, buffer.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, c);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        itemCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energy.getEnergyStored());
        tag.put("buffer", buffer.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        buffer.deserializeNBT(tag.getCompound("buffer"));
    }
}
