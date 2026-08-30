/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import buildcraft.energy.tile.TileEngineBase;

/**
 * Generic engine block: a directional machine backed by a {@link TileEngineBase}. Placement points
 * the trunk towards the clicked face; right-click opens a GUI if the engine has one.
 */
public class BlockEngine<T extends TileEngineBase> extends DirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    private final Supplier<BlockEntityType<T>> typeSupplier;
    private final BiFunction<BlockPos, BlockState, T> factory;

    public BlockEngine(Properties props, Supplier<BlockEntityType<T>> typeSupplier,
            BiFunction<BlockPos, BlockState, T> factory) {
        super(props);
        this.typeSupplier = typeSupplier;
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider provider && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, provider, pos);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof MenuProvider ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEngineBase engine) {
                engine.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, moving);
        }
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
