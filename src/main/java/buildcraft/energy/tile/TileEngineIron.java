/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import buildcraft.energy.fluid.BCFluids;
import buildcraft.registry.BCBlockEntities;

/**
 * Combustion Engine (historically {@code engineIron}): the high-tier generator. It burns liquid Fuel
 * (piped in from a Refinery) for a strong, continuous output, and also accepts solid furnace fuel as
 * a fallback.
 */
public class TileEngineIron extends TileEngineFuel {
    public static final int CAPACITY = 100_000;
    public static final int OUTPUT = 200;
    public static final int GENERATION = 40;

    public static final int FUEL_TANK_CAPACITY = 8_000;
    public static final int FLUID_CONSUME_PER_TICK = 2;
    public static final int FLUID_GENERATION = 100;

    private final FluidTank fuelTank = new FluidTank(FUEL_TANK_CAPACITY, fs -> fs.getFluid() == BCFluids.FUEL.get());
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> fuelTank);

    public TileEngineIron(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ENGINE_IRON.get(), pos, state, CAPACITY, OUTPUT);
    }

    @Override
    protected int getGenerationRate() {
        return GENERATION;
    }

    @Override
    protected int getOutputRate() {
        return OUTPUT;
    }

    @Override
    protected void tickEngine(Level level, BlockPos pos, BlockState state) {
        super.tickEngine(level, pos, state);
        // Burn liquid fuel continuously while powered.
        if (isRedstonePowered(level, pos) && fuelTank.getFluidAmount() >= FLUID_CONSUME_PER_TICK) {
            if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                fuelTank.drain(FLUID_CONSUME_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
                energy.generate(FLUID_GENERATION);
            }
            setChanged();
        }
    }

    @Override
    protected boolean isActivelyGenerating() {
        return super.isActivelyGenerating()
                || (level != null && isRedstonePowered(level, worldPosition)
                && fuelTank.getFluidAmount() >= FLUID_CONSUME_PER_TICK);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fuelTank", fuelTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelTank.readFromNBT(tag.getCompound("fuelTank"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.engine_iron");
    }
}
