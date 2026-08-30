/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** The Wrench: right-click a machine to rotate it (re-aim engines, lasers, gates, builders, ...). */
public class ItemWrench extends Item {

    public ItemWrench(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        BlockState rotated = null;

        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            rotated = state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise());
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction current = state.getValue(BlockStateProperties.FACING);
            Direction next = Direction.from3DDataValue((current.get3DDataValue() + 1) % 6);
            rotated = state.setValue(BlockStateProperties.FACING, next);
        }

        if (rotated == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(context.getClickedPos(), rotated, 3);
            level.updateNeighborsAt(context.getClickedPos(), rotated.getBlock());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
