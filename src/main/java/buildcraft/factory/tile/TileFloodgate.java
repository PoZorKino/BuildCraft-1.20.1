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
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import buildcraft.registry.BCBlockEntities;

/**
 * Floodgate: places fluid source blocks into the world around and below it, draining its internal
 * tank (filled by pipes). Handy for flooding an area with water/oil.
 */
public class TileFloodgate extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 16_000;
    public static final int INTERVAL = 4;
    private static final Direction[] SPREAD = {
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private final FluidTank tank = new FluidTank(CAPACITY);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);
    private int cooldown;

    public TileFloodgate(BlockPos pos, BlockState state) {
        super(BCBlockEntities.FLOODGATE.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        FluidStack fluid = tank.getFluid();
        if (fluid.getAmount() < 1000) {
            return;
        }
        Fluid f = fluid.getFluid();
        if (!(f instanceof FlowingFluid flowing)) {
            return;
        }
        for (Direction dir : SPREAD) {
            BlockPos target = pos.relative(dir);
            if (level.getBlockState(target).isAir()) {
                level.setBlock(target, flowing.getSource().defaultFluidState().createLegacyBlock(), 3);
                tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                cooldown = INTERVAL;
                setChanged();
                return;
            }
        }
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
        tag.put("tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tank.readFromNBT(tag.getCompound("tank"));
    }
}
