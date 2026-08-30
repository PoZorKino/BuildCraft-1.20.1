/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import buildcraft.transport.tile.TilePipe;

/**
 * A transport pipe block. Reuses vanilla {@link PipeBlock}'s six boolean connection properties and
 * generated arm shapes; connections form to neighbouring pipes and to any block exposing an item
 * handler capability.
 */
public class BlockPipe<T extends TilePipe> extends PipeBlock implements EntityBlock {

    private final Supplier<BlockEntityType<T>> typeSupplier;
    private final BiFunction<BlockPos, BlockState, T> factory;

    public BlockPipe(Properties props, Supplier<BlockEntityType<T>> typeSupplier,
            BiFunction<BlockPos, BlockState, T> factory) {
        super(0.25F, props);
        this.typeSupplier = typeSupplier;
        this.factory = factory;
        BlockState def = stateDefinition.any();
        for (BooleanProperty p : PROPERTY_BY_DIRECTION.values()) {
            def = def.setValue(p, false);
        }
        registerDefaultState(def);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    public static boolean isConnected(BlockState state, Direction dir) {
        BooleanProperty prop = PROPERTY_BY_DIRECTION.get(dir);
        return prop != null && state.hasProperty(prop) && state.getValue(prop);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction dir : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(dir), canConnect(level, pos, dir));
        }
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(dir), canConnect(level, pos, dir));
    }

    private boolean canConnect(BlockGetter level, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        if (level.getBlockState(neighborPos).getBlock() instanceof BlockPipe) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(neighborPos);
        return be != null
                && be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).isPresent();
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Override
    @Nullable
    public <A extends BlockEntity> BlockEntityTicker<A> getTicker(net.minecraft.world.level.Level level,
            BlockState state, BlockEntityType<A> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, typeSupplier.get(),
                (lvl, pos, st, be) -> be.serverTick(lvl, pos, st));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
        return expected == given ? (BlockEntityTicker<A>) ticker : null;
    }
}
