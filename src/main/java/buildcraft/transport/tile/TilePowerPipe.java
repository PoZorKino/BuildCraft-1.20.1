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

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockPipe;
import buildcraft.transport.pipe.IPipeHolder;
import buildcraft.transport.pipe.PipeSideState;

/**
 * Power (kinesis) pipe: receives energy from engines/pipes and transmits it towards connected energy
 * consumers, balancing along the pipe network. Placing one next to an engine lets it distribute the
 * engine's output to machines further away.
 */
public class TilePowerPipe extends BlockEntity implements ITickingMachine, IPipeHolder {

    public static final int CAPACITY = 10_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int FLOW_RATE = 500;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final PipeSideState sides = new PipeSideState(this);

    public TilePowerPipe(BlockPos pos, BlockState state) {
        this(BCBlockEntities.PIPE_POWER_COBBLESTONE.get(), pos, state);
    }

    public TilePowerPipe(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public PipeSideState sides() {
        return sides;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (energy.getEnergyStored() <= 0) {
            return;
        }
        // Phase 1: deliver to non-pipe energy consumers.
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir) || energy.getEnergyStored() <= 0) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof TilePowerPipe) {
                continue;
            }
            IEnergyStorage dest = neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).orElse(null);
            if (dest != null && dest.canReceive()) {
                int send = Math.min(FLOW_RATE, energy.getEnergyStored());
                int accepted = dest.receiveEnergy(send, false);
                if (accepted > 0) {
                    energy.spend(accepted);
                }
            }
        }
        // Phase 2: balance into neighbouring power pipes with less stored.
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir) || energy.getEnergyStored() <= 0) {
                continue;
            }
            if (level.getBlockEntity(pos.relative(dir)) instanceof TilePowerPipe pipe) {
                int diff = energy.getEnergyStored() - pipe.energy.getEnergyStored();
                if (diff > 1) {
                    int move = Math.min(Math.min(FLOW_RATE, diff / 2), energy.getEnergyStored());
                    int accepted = pipe.energy.receiveEnergy(move, false);
                    if (accepted > 0) {
                        energy.spend(accepted);
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
        sides.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        sides.load(tag);
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
