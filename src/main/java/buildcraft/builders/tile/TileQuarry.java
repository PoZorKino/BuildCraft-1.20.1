/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/**
 * Quarry: an automated area miner. Consumes energy to strip-mine a square area centred on itself,
 * layer by layer from the top down, pushing everything it collects into an adjacent inventory (or
 * dropping it on top if none is attached).
 */
public class TileQuarry extends BlockEntity implements ITickingMachine {

    public static final int CAPACITY = 100_000;
    public static final int MAX_RECEIVE = 2_000;
    public static final int COST_PER_BLOCK = 300;
    public static final int MINE_INTERVAL = 4;
    /** Half-width of the mined square, so the full area is (2*RADIUS+1) x (2*RADIUS+1). */
    public static final int RADIUS = 4;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

    private int currentY = Integer.MAX_VALUE;
    private int scanIndex;
    private boolean finished;
    private int cooldown;

    public TileQuarry(BlockPos pos, BlockState state) {
        super(BCBlockEntities.QUARRY.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (currentY == Integer.MAX_VALUE) {
            currentY = pos.getY() - 1;
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

        int side = 2 * RADIUS + 1;
        int perLayer = side * side;
        int minY = server.getMinBuildHeight();

        while (currentY >= minY) {
            if (scanIndex >= perLayer) {
                scanIndex = 0;
                currentY--;
                continue;
            }
            int dx = (scanIndex % side) - RADIUS;
            int dz = (scanIndex / side) - RADIUS;
            scanIndex++;

            BlockPos target = new BlockPos(pos.getX() + dx, currentY, pos.getZ() + dz);
            BlockState targetState = server.getBlockState(target);
            if (targetState.isAir() || !server.getFluidState(target).isEmpty()) {
                continue;
            }
            float hardness = targetState.getDestroySpeed(server, target);
            if (hardness < 0) {
                continue; // Skip unbreakable blocks (e.g. bedrock) rather than stopping entirely.
            }
            if (!energy.spend(COST_PER_BLOCK)) {
                return;
            }
            List<ItemStack> drops = Block.getDrops(targetState, server, target, server.getBlockEntity(target));
            server.destroyBlock(target, false);
            outputDrops(server, pos, drops);
            cooldown = MINE_INTERVAL;
            setChanged();
            return;
        }
        finished = true;
    }

    private void outputDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            ItemStack remaining = drop;
            for (Direction dir : Direction.values()) {
                if (remaining.isEmpty()) {
                    break;
                }
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor == null) {
                    continue;
                }
                IItemHandler handler = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
                if (handler != null) {
                    remaining = ItemHandlerHelper.insertItem(handler, remaining, false);
                }
            }
            if (!remaining.isEmpty()) {
                Block.popResource(level, pos.above(), remaining);
            }
        }
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public boolean isFinished() {
        return finished;
    }

    public AABB getMiningArea() {
        return new AABB(
                getBlockPos().offset(-RADIUS, -1, -RADIUS),
                getBlockPos().offset(RADIUS + 1, 0, RADIUS + 1));
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
        tag.putInt("currentY", currentY);
        tag.putInt("scanIndex", scanIndex);
        tag.putBoolean("finished", finished);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        currentY = tag.contains("currentY") ? tag.getInt("currentY") : Integer.MAX_VALUE;
        scanIndex = tag.getInt("scanIndex");
        finished = tag.getBoolean("finished");
    }
}
