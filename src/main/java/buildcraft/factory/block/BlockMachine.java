/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.tile.TileChute;

/** Generic solid machine block backed by a block entity, with an optional server-side ticker. */
public class BlockMachine<T extends BlockEntity> extends Block implements EntityBlock {

    private final Supplier<BlockEntityType<T>> typeSupplier;
    private final BiFunction<BlockPos, BlockState, T> factory;

    public BlockMachine(Properties props, Supplier<BlockEntityType<T>> typeSupplier,
            BiFunction<BlockPos, BlockState, T> factory) {
        super(props);
        this.typeSupplier = typeSupplier;
        this.factory = factory;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileChute chute) {
                chute.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Override
    @Nullable
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(Level level, BlockState state,
            BlockEntityType<A> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, typeSupplier.get(), (lvl, pos, st, be) -> {
            if (be instanceof ITickingMachine machine) {
                machine.serverTick(lvl, pos, st);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
        return expected == given ? (BlockEntityTicker<A>) ticker : null;
    }
}
