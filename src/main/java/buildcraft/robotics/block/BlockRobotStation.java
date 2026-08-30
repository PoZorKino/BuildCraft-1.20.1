/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.factory.block.BlockMachine;
import buildcraft.robotics.tile.TileRobotStation;

/** Robot Station block: drops its collected-item buffer when broken. */
public class BlockRobotStation extends BlockMachine<TileRobotStation> {

    public BlockRobotStation(Properties props, Supplier<BlockEntityType<TileRobotStation>> typeSupplier,
            BiFunction<BlockPos, BlockState, TileRobotStation> factory) {
        super(props, typeSupplier, factory);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileRobotStation station) {
                station.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }
}
