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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import buildcraft.energy.fluid.BCFluids;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Refinery: consumes energy to distil Oil into Fuel. Oil is filled into the input tank (by a Pump or
 * pipe); refined Fuel accumulates in the output tank and can be drained out.
 */
public class TileRefinery extends BlockEntity implements ITickingMachine {

    public static final int TANK_CAPACITY = 8_000;
    public static final int ENERGY_CAPACITY = 40_000;
    public static final int MAX_RECEIVE = 1_000;

    public static final int OIL_PER_OP = 10;
    public static final int FUEL_PER_OP = 8;
    public static final int ENERGY_PER_TICK = 40;
    public static final int TICKS_PER_OP = 40;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE);
    private final FluidTank oilTank = new FluidTank(TANK_CAPACITY, fs -> fs.getFluid() == BCFluids.OIL.get());
    private final FluidTank fuelTank = new FluidTank(TANK_CAPACITY);

    private final RefineryFluidHandler fluidHandler = new RefineryFluidHandler();
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fluidHandler);

    private int progress;

    public TileRefinery(BlockPos pos, BlockState state) {
        super(BCBlockEntities.REFINERY.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean canRun = oilTank.getFluidAmount() >= OIL_PER_OP
                && fuelTank.getFluidAmount() + FUEL_PER_OP <= fuelTank.getCapacity()
                && energy.getEnergyStored() >= ENERGY_PER_TICK;

        if (canRun && energy.spend(ENERGY_PER_TICK)) {
            progress++;
            if (progress >= TICKS_PER_OP) {
                progress = 0;
                oilTank.drain(OIL_PER_OP, IFluidHandler.FluidAction.EXECUTE);
                fuelTank.fill(new FluidStack(BCFluids.FUEL.get(), FUEL_PER_OP), IFluidHandler.FluidAction.EXECUTE);
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public FluidStack getOil() {
        return oilTank.getFluid();
    }

    public FluidStack getFuel() {
        return fuelTank.getFluid();
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
        tag.put("oil", oilTank.writeToNBT(new CompoundTag()));
        tag.put("fuel", fuelTank.writeToNBT(new CompoundTag()));
        tag.putInt("progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        oilTank.readFromNBT(tag.getCompound("oil"));
        fuelTank.readFromNBT(tag.getCompound("fuel"));
        progress = tag.getInt("progress");
    }

    /** Exposes oil as the fillable input (tank 0) and fuel as the drainable output (tank 1). */
    private class RefineryFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? oilTank.getFluid() : fuelTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tank == 0 && stack.getFluid() == BCFluids.OIL.get();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid() != BCFluids.OIL.get()) {
                return 0;
            }
            return oilTank.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid() != BCFluids.FUEL.get()) {
                return FluidStack.EMPTY;
            }
            return fuelTank.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return fuelTank.drain(maxDrain, action);
        }
    }
}
