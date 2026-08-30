/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Pump: consumes energy to draw fluid source blocks from the column below into an internal tank, then
 * pushes that fluid into adjacent fluid handlers (such as Tanks).
 */
public class TilePump extends BlockEntity implements ITickingMachine {

    public static final int ENERGY_CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int TANK_CAPACITY = 16_000;
    public static final int COST_PER_BUCKET = 1_000;
    public static final int OUTPUT_PER_TICK = 200;
    public static final int PUMP_INTERVAL = 10;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE);
    private final FluidTank tank = new FluidTank(TANK_CAPACITY);

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    private int nextY = Integer.MAX_VALUE;
    private int cooldown;

    public TilePump(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PUMP.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        pushToNeighbours(level, pos);

        if (nextY == Integer.MAX_VALUE) {
            nextY = pos.getY() - 1;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < COST_PER_BUCKET) {
            return;
        }
        if (tank.getFluidAmount() + 1000 > tank.getCapacity()) {
            return;
        }

        int minY = level.getMinBuildHeight();
        int guard = 0;
        int y = nextY;
        while (y >= minY && guard++ < 512) {
            BlockPos target = new BlockPos(pos.getX(), y, pos.getZ());
            FluidState fluidState = level.getFluidState(target);
            if (fluidState.isEmpty() || !fluidState.isSource()) {
                y--;
                continue;
            }
            BlockState targetState = level.getBlockState(target);
            if (!(targetState.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock)
                    && !targetState.canBeReplaced()) {
                y--;
                continue;
            }
            Fluid fluid = fluidState.getType();
            FluidStack drained = new FluidStack(fluid, 1000);
            if (tank.fill(drained, IFluidHandler.FluidAction.SIMULATE) != 1000) {
                y--;
                continue;
            }
            if (!energy.spend(COST_PER_BUCKET)) {
                return;
            }
            tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
            level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
            nextY = y - 1;
            cooldown = PUMP_INTERVAL;
            setChanged();
            return;
        }
        // Nothing left in this column; keep the last position so it stops scanning uselessly.
        nextY = minY - 1;
    }

    private void pushToNeighbours(Level level, BlockPos pos) {
        if (tank.getFluidAmount() <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) {
                continue;
            }
            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(dest ->
                    FluidUtil.tryFluidTransfer(dest, tank, OUTPUT_PER_TICK, true));
        }
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        fluidCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energy.getEnergyStored());
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
        tag.putInt("nextY", nextY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        tank.readFromNBT(tag.getCompound("tank"));
        nextY = tag.contains("nextY") ? tag.getInt("nextY") : Integer.MAX_VALUE;
    }
}
