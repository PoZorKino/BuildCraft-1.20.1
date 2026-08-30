/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.nbt.NBTUtilBC;
import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockPipe;

/**
 * Iron transport pipe: a directional pipe that only emits items out of a single chosen face.
 * Right-click with a Wrench to cycle the output among currently connected sides.
 */
public class TilePipeIron extends TilePipe {

    @Nullable
    private Direction output;

    public TilePipeIron(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE_IRON.get(), pos, state, 8);
    }

    @Nullable
    public Direction getOutput() {
        return output;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (output == null || !BlockPipe.isConnected(state, output)) {
            Direction next = firstConnected(state);
            if (next != output) {
                output = next;
                setChanged();
            }
        }
        super.serverTick(level, pos, state);
    }

    /** Cycle the output face among currently connected sides. Returns the new facing (or null). */
    @Nullable
    public Direction cycleOutput() {
        BlockState state = getBlockState();
        Direction start = output == null ? Direction.DOWN : output;
        for (int i = 1; i <= 6; i++) {
            Direction next = Direction.from3DDataValue((start.get3DDataValue() + i) % 6);
            if (BlockPipe.isConnected(state, next)) {
                output = next;
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, state, state, 3);
                }
                return output;
            }
        }
        output = null;
        setChanged();
        return null;
    }

    @Nullable
    private static Direction firstConnected(BlockState state) {
        for (Direction dir : Direction.values()) {
            if (BlockPipe.isConnected(state, dir)) {
                return dir;
            }
        }
        return null;
    }

    @Override
    protected List<Direction> collectOutputs(BlockState state, TravelingItem ti) {
        List<Direction> outputs = new ArrayList<>();
        if (output != null && BlockPipe.isConnected(state, output) && output != ti.from) {
            outputs.add(output);
        }
        // Iron pipes bounce back if the chosen face is the entry face or is unavailable.
        if (outputs.isEmpty() && BlockPipe.isConnected(state, ti.from)) {
            outputs.add(ti.from);
        }
        return outputs;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (output != null) {
            tag.put("output", NBTUtilBC.writeDirection(output));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        output = tag.contains("output") ? NBTUtilBC.readDirection(tag.get("output")) : null;
    }
}
