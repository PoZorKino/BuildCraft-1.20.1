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

/**
 * Combustion Engine (historically {@code engineIron}): the high-tier fuel burner. The classic engine
 * ran on liquid fuel with water cooling; this port simplifies it to a high-output solid-fuel burner
 * until the fluid system is ported.
 */
public class TileEngineIron extends TileEngineFuel {
    public static final int CAPACITY = 100_000;
    public static final int OUTPUT = 200;
    public static final int GENERATION = 40;

    public TileEngineIron(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ENGINE_IRON.get(), pos, state, CAPACITY, OUTPUT);
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
        return Component.translatable("block.buildcraft.engine_iron");
    }
}
