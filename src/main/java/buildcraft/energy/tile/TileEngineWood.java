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
 * Redstone Engine (historically {@code engineWood}): the entry-level engine. Produces a trickle of
 * energy whenever it receives a redstone signal, needs no fuel, and cannot overheat.
 */
public class TileEngineWood extends TileEngineBase {
    public static final int CAPACITY = 1_000;
    public static final int OUTPUT = 10;
    public static final int GENERATION = 1;

    public TileEngineWood(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ENGINE_WOOD.get(), pos, state, CAPACITY, OUTPUT);
    }

    @Override
    protected void tickEngine(Level level, BlockPos pos, BlockState state) {
        if (isRedstonePowered(level, pos)) {
            energy.generate(GENERATION);
        }
    }

    @Override
    protected int getOutputRate() {
        return OUTPUT;
    }

    /** The Redstone Engine never overheats, so cap its displayed stage below overheat. */
    @Override
    public int getPowerStage() {
        return Math.min(3, super.getPowerStage());
    }

    @Override
    protected boolean canOverheat() {
        return false;
    }

    @Override
    protected boolean isActivelyGenerating() {
        return level != null && isRedstonePowered(level, worldPosition);
    }
}
