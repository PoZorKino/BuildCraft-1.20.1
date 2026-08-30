/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;

/** Void pipe: any item that enters it is destroyed. A convenient trash disposal for item networks. */
public class TilePipeVoid extends TilePipe {

    public TilePipeVoid(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE_VOID.get(), pos, state, 8);
    }

    @Override
    public void accept(ItemStack stack, Direction from) {
        // Silently consume everything routed into a void pipe.
    }
}
