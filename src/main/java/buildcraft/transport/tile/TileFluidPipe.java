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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockPipe;

/**
 * Fluid transport pipe: buffers a small amount of fluid and flows it towards connected fluid
 * handlers (tanks, machines) and, failing that, balances it into neighbouring fluid pipes so it
 * travels along the network.
 */
public class TileFluidPipe extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 2_000;
    public static final int FLOW_RATE = 100;

    protected final FluidTank tank = new FluidTank(CAPACITY);
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);

    public TileFluidPipe(BlockPos pos, BlockState state) {
        this(BCBlockEntities.PIPE_FLUID_COBBLESTONE.get(), pos, state);
    }

    public TileFluidPipe(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (tank.getFluidAmount() <= 0) {
            return;
        }
        // Phase 1: drain into non-pipe fluid handlers (tanks, machines).
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir)) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof TileFluidPipe) {
                continue;
            }
            neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(dest ->
                    FluidUtil.tryFluidTransfer(dest, tank, FLOW_RATE, true));
        }
        // Phase 2: balance into neighbouring pipes that hold less, so fluid flows along the network.
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir) || tank.getFluidAmount() <= 0) {
                continue;
            }
            if (level.getBlockEntity(pos.relative(dir)) instanceof TileFluidPipe pipe) {
                int diff = tank.getFluidAmount() - pipe.tank.getFluidAmount();
                if (diff > 1) {
                    int move = Math.min(Math.min(FLOW_RATE, diff / 2), tank.getFluidAmount());
                    FluidStack drained = tank.drain(move, IFluidHandler.FluidAction.EXECUTE);
                    if (!drained.isEmpty()) {
                        int filled = pipe.tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                        if (filled < drained.getAmount()) {
                            drained.setAmount(drained.getAmount() - filled);
                            tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                        }
                        pipe.setChanged();
                    }
                }
            }
        }
        setChanged();
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
