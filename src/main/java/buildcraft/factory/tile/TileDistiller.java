/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;

/**
 * Distiller: distils Oil into Fuel using energy, sharing the Refinery's fluid-processing logic. In
 * BuildCraft 8 the distiller is the compact single-block refining machine.
 */
public class TileDistiller extends TileRefinery {

    public TileDistiller(BlockPos pos, BlockState state) {
        super(BCBlockEntities.DISTILLER.get(), pos, state);
    }
}
