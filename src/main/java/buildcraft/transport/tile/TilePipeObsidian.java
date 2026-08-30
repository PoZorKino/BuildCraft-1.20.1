/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import buildcraft.registry.BCBlockEntities;

/** Obsidian pipe: sucks up nearby dropped items and injects them into the pipe network. */
public class TilePipeObsidian extends TilePipe {

    public static final double RADIUS = 3.0;

    public TilePipeObsidian(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE_OBSIDIAN.get(), pos, state, 8);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);
        AABB area = new AABB(pos).inflate(RADIUS);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area,
                e -> e.isAlive() && !e.getItem().isEmpty());
        for (ItemEntity entity : drops) {
            accept(entity.getItem().copy(), Direction.UP);
            entity.discard();
        }
    }
}
