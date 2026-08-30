/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockPipe;

/** Wooden fluid pipe: uses engine energy to pull fluid out of an adjacent tank/machine. */
public class TileFluidPipeWood extends TileFluidPipe {

    public static final int CAPACITY = 5_000;
    public static final int MAX_RECEIVE = 500;
    public static final int COST_PER_OP = 20;
    public static final int EXTRACT_INTERVAL = 5;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int cooldown;

    public TileFluidPipeWood(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE_FLUID_WOOD.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);

        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < COST_PER_OP || tank.getFluidAmount() >= CAPACITY) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir)) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof TileFluidPipe) {
                continue;
            }
            net.minecraftforge.fluids.capability.IFluidHandler source =
                    neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).orElse(null);
            if (source != null) {
                var moved = FluidUtil.tryFluidTransfer(tank, source, FLOW_RATE, true);
                if (!moved.isEmpty()) {
                    energy.spend(COST_PER_OP);
                    cooldown = EXTRACT_INTERVAL;
                    return;
                }
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            if (side != null && isSideBlocked(side)) {
                return LazyOptional.empty();
            }
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
    }
}
