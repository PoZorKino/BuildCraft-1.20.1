/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.tile;

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

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;
import buildcraft.silicon.block.BlockLaser;

/**
 * Laser: receives energy and beams it into the machine it faces (typically an Assembly Table),
 * acting as a compact, high-throughput power relay.
 */
public class TileLaser extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 2_000;
    public static final int OUTPUT_RATE = 1_000;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    public TileLaser(BlockPos pos, BlockState state) {
        super(BCBlockEntities.LASER.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (energy.getEnergyStored() <= 0) {
            return;
        }
        Direction facing = state.getValue(BlockLaser.FACING);
        BlockEntity target = level.getBlockEntity(pos.relative(facing));
        if (target == null) {
            return;
        }
        IEnergyStorage dest = target.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite()).orElse(null);
        if (dest != null && dest.canReceive()) {
            int accepted = dest.receiveEnergy(Math.min(OUTPUT_RATE, energy.getEnergyStored()), false);
            if (accepted > 0) {
                energy.spend(accepted);
                setChanged();
            }
        }
        // Sync so the client can show/hide the beam based on activity.
        level.sendBlockUpdated(pos, state, state, 3);
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
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }
}

