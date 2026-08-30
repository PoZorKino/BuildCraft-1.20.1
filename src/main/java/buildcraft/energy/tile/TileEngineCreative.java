/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;

/**
 * Creative Engine: an infinite, always-on energy source for testing and creative builds. Keeps its
 * buffer topped up every tick regardless of redstone or fuel.
 */
public class TileEngineCreative extends TileEngineBase {
    public static final int CAPACITY = 1_000_000;
    public static final int OUTPUT = 10_000;

    public TileEngineCreative(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ENGINE_CREATIVE.get(), pos, state, CAPACITY, OUTPUT);
    }

    @Override
    protected void tickEngine(Level level, BlockPos pos, BlockState state) {
        energy.generate(CAPACITY);
    }

    @Override
    protected int getOutputRate() {
        return OUTPUT;
    }

    @Override
    protected boolean canOverheat() {
        return false;
    }

    @Override
    protected boolean isActivelyGenerating() {
        return true;
    }
}
