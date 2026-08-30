/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import buildcraft.builders.BlueprintData;
import buildcraft.builders.block.BlockBuilderMachine;
import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Filler: fills the cuboid in front of it with blocks pulled from an adjacent inventory, one block
 * per cycle, using energy. A simple stand-in for BuildCraft's pattern-based filler.
 */
public class TileFiller extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 30_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int FILL_COST = 100;
    public static final int FILL_INTERVAL = 4;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int fillIndex;
    private int cooldown;

    public TileFiller(BlockPos pos, BlockState state) {
        super(BCBlockEntities.FILLER.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < FILL_COST) {
            return;
        }
        int volume = BlueprintData.SIZE * BlueprintData.SIZE * BlueprintData.SIZE;
        if (fillIndex >= volume) {
            return;
        }
        Direction facing = state.getValue(BlockBuilderMachine.FACING);
        BlockPos origin = BlueprintData.regionOrigin(pos, facing);

        while (fillIndex < volume) {
            int i = fillIndex;
            int dz = i % BlueprintData.SIZE;
            int dy = (i / BlueprintData.SIZE) % BlueprintData.SIZE;
            int dx = i / (BlueprintData.SIZE * BlueprintData.SIZE);
            BlockPos target = origin.offset(dx, dy, dz);
            fillIndex++;
            if (target.equals(getBlockPos()) || !level.getBlockState(target).canBeReplaced()) {
                continue;
            }
            BlockState toPlace = pullBlockFromNeighbour(level, pos);
            if (toPlace == null) {
                fillIndex--; // No materials right now; retry this cell next time.
                return;
            }
            level.setBlock(target, toPlace, 3);
            energy.spend(FILL_COST);
            cooldown = FILL_INTERVAL;
            setChanged();
            return;
        }
        setChanged();
    }

    @Nullable
    private BlockState pullBlockFromNeighbour(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) {
                continue;
            }
            IItemHandler handler = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            if (handler == null) {
                continue;
            }
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.getItem() instanceof BlockItem blockItem) {
                    ItemStack taken = handler.extractItem(slot, 1, false);
                    if (!taken.isEmpty()) {
                        return blockItem.getBlock().defaultBlockState();
                    }
                }
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("fillIndex", fillIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        fillIndex = tag.getInt("fillIndex");
    }
}
