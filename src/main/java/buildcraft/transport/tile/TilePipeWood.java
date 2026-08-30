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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockPipe;

/**
 * Wooden transport pipe: the network's extractor. When supplied with engine energy it pulls items
 * out of an adjacent inventory and injects them into the pipe network.
 */
public class TilePipeWood extends TilePipe {

    public static final int CAPACITY = 5_000;
    public static final int MAX_RECEIVE = 500;
    public static final int COST_PER_EXTRACT = 100;
    public static final int EXTRACT_COUNT = 4;
    public static final int EXTRACT_INTERVAL = 10;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private int cooldown;

    public TilePipeWood(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE_WOOD.get(), pos, state, 8);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);

        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < COST_PER_EXTRACT) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir)) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof TilePipe) {
                continue;
            }
            IItemHandler handler = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            if (handler == null) {
                continue;
            }
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack extracted = handler.extractItem(slot, EXTRACT_COUNT, false);
                if (!extracted.isEmpty()) {
                    accept(extracted, dir);
                    energy.spend(COST_PER_EXTRACT);
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
