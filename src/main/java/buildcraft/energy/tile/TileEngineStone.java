/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;

/** Stirling Engine (historically {@code engineStone}): a mid-tier solid-fuel generator. */
public class TileEngineStone extends TileEngineFuel {
    public static final int CAPACITY = 10_000;
    public static final int OUTPUT = 40;
    public static final int GENERATION = 10;

    public TileEngineStone(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ENGINE_STONE.get(), pos, state, CAPACITY, OUTPUT);
    }

    @Override
    protected int getGenerationRate() {
        return GENERATION;
    }

    @Override
    protected int getOutputRate() {
        return OUTPUT;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.engine_stone");
    }
}
