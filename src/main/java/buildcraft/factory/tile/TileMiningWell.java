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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Mining Well: consumes energy to dig a shaft straight down, dropping everything it mines. A simple
 * standalone version of BuildCraft's classic automated miner.
 */
public class TileMiningWell extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int COST_PER_BLOCK = 200;
    /** How often (in ticks) a block may be mined, so it is visible rather than instant. */
    public static final int MINE_INTERVAL = 8;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private int nextY = Integer.MAX_VALUE;
    private boolean finished;
    private int cooldown;

    public TileMiningWell(BlockPos pos, BlockState state) {
        super(BCBlockEntities.MINING_WELL.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (nextY == Integer.MAX_VALUE) {
            nextY = pos.getY() - 1;
        }
        if (finished) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < COST_PER_BLOCK) {
            return;
        }

        // Find the next non-air, breakable block below (skipping air gaps and fluids).
        int minY = server.getMinBuildHeight();
        int guard = 0;
        while (nextY >= minY && guard++ < 512) {
            BlockPos target = new BlockPos(pos.getX(), nextY, pos.getZ());
            BlockState targetState = server.getBlockState(target);
            FluidState fluidState = server.getFluidState(target);
            if (targetState.isAir() || !fluidState.isEmpty()) {
                nextY--;
                continue;
            }
            float hardness = targetState.getDestroySpeed(server, target);
            if (hardness < 0) {
                // Unbreakable (e.g. bedrock) - stop the well.
                finished = true;
                return;
            }
            if (!energy.spend(COST_PER_BLOCK)) {
                return;
            }
            // Drop the mined resources on top of the well so they are not re-mined.
            Block.dropResources(targetState, server, pos.above(), server.getBlockEntity(target));
            server.destroyBlock(target, false);
            nextY--;
            cooldown = MINE_INTERVAL;
            setChanged();
            return;
        }
        finished = true;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
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
        tag.putInt("nextY", nextY);
        tag.putBoolean("finished", finished);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        nextY = tag.contains("nextY") ? tag.getInt("nextY") : Integer.MAX_VALUE;
        finished = tag.getBoolean("finished");
    }
}
