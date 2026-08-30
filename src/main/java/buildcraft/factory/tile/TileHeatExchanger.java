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
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import buildcraft.energy.util.EngineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Heat Exchanger: draws heat from Lava in its tank and converts it into Forge Energy, pushing that
 * energy into adjacent machines. A modern single-block interpretation of BuildCraft's coolant
 * heat-exchange system.
 */
public class TileHeatExchanger extends BlockEntity implements ITickingMachine {

    public static final int TANK_CAPACITY = 8_000;
    public static final int ENERGY_CAPACITY = 40_000;
    public static final int OUTPUT_RATE = 200;
    public static final int LAVA_PER_OP = 50;
    public static final int ENERGY_PER_OP = 200;

    private final FluidTank lavaTank = new FluidTank(TANK_CAPACITY, fs -> fs.getFluid() == Fluids.LAVA);
    private final EngineEnergyStorage energy = new EngineEnergyStorage(ENERGY_CAPACITY, OUTPUT_RATE);

    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> lavaTank);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    public TileHeatExchanger(BlockPos pos, BlockState state) {
        super(BCBlockEntities.HEAT_EXCHANGER.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (lavaTank.getFluidAmount() >= LAVA_PER_OP
                && energy.getEnergyStored() + ENERGY_PER_OP <= energy.getMaxEnergyStored()) {
            lavaTank.drain(LAVA_PER_OP, IFluidHandler.FluidAction.EXECUTE);
            energy.generate(ENERGY_PER_OP);
            setChanged();
        }
        pushEnergy(level, pos);
    }

    private void pushEnergy(Level level, BlockPos pos) {
        if (energy.getEnergyStored() <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) {
                continue;
            }
            IEnergyStorage dest = neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).orElse(null);
            if (dest != null && dest.canReceive()) {
                int accepted = dest.receiveEnergy(Math.min(OUTPUT_RATE, energy.getEnergyStored()), false);
                if (accepted > 0) {
                    energy.consume(accepted);
                    setChanged();
                }
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
        energyCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("lava", lavaTank.writeToNBT(new CompoundTag()));
        tag.putInt("energy", energy.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lavaTank.readFromNBT(tag.getCompound("lava"));
        energy.setEnergyStored(tag.getInt("energy"));
    }
}
