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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import buildcraft.energy.block.BlockEngine;
import buildcraft.energy.util.EngineEnergyStorage;

/**
 * Shared behaviour for all BuildCraft engines: an internal energy buffer that a subclass fills each
 * tick, which is then pushed out of the face the engine points towards. Also exposes the classic
 * blue/green/yellow/red/overheat power stages based on the fill ratio.
 */
public abstract class TileEngineBase extends BlockEntity {

    protected final EngineEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyCap;

    protected TileEngineBase(BlockEntityType<?> type, BlockPos pos, BlockState state,
            int capacity, int maxOutput) {
        super(type, pos, state);
        this.energy = new EngineEnergyStorage(capacity, maxOutput);
        this.energyCap = LazyOptional.of(() -> energy);
    }

    public final void serverTick(Level level, BlockPos pos, BlockState state) {
        tickEngine(level, pos, state);
        pushEnergy(level, pos, state);
        setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    /** Subclasses generate energy here (into {@link #energy}). */
    protected abstract void tickEngine(Level level, BlockPos pos, BlockState state);

    /** Maximum energy pushed to the connected machine each tick. */
    protected abstract int getOutputRate();

    protected boolean isRedstonePowered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos);
    }

    private void pushEnergy(Level level, BlockPos pos, BlockState state) {
        if (energy.getEnergyStored() <= 0) {
            return;
        }
        Direction facing = state.getValue(BlockEngine.FACING);
        BlockEntity neighbor = level.getBlockEntity(pos.relative(facing));
        if (neighbor == null) {
            return;
        }
        neighbor.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite()).ifPresent(target -> {
            if (!target.canReceive()) {
                return;
            }
            int toOffer = Math.min(getOutputRate(), energy.getEnergyStored());
            int accepted = target.receiveEnergy(toOffer, false);
            if (accepted > 0) {
                energy.consume(accepted);
            }
        });
    }

    /** 0..4 mapped from stored-energy ratio (blue, green, yellow, red, overheat). */
    public int getPowerStage() {
        double ratio = energy.getEnergyStored() / (double) energy.getMaxEnergyStored();
        if (ratio < 0.25) return 0;
        if (ratio < 0.50) return 1;
        if (ratio < 0.75) return 2;
        if (ratio < 1.00) return 3;
        return 4;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    public void dropContents(Level level, BlockPos pos) {
        // No item inventory by default.
    }

    // --- Capabilities -------------------------------------------------------

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

    // --- NBT / sync ---------------------------------------------------------

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

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }
}
