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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.network.NetworkHooks;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.transport.tile.TilePipeDiamond;

/**
 * A transport pipe block. Reuses vanilla {@link PipeBlock}'s six boolean connection properties and
 * generated arm shapes. Pipes connect to same-kind pipes and to any neighbouring block exposing the
 * relevant capability (items, fluids, or energy), depending on the pipe's {@link #kind}.
 */
public class BlockPipe<T extends BlockEntity> extends PipeBlock implements EntityBlock {

    private final Supplier<BlockEntityType<T>> typeSupplier;
    private final BiFunction<BlockPos, BlockState, T> factory;
    private final Supplier<Capability<?>> connectCap;
    private final String kind;

    public BlockPipe(Properties props, Supplier<BlockEntityType<T>> typeSupplier,
            BiFunction<BlockPos, BlockState, T> factory, Supplier<Capability<?>> connectCap, String kind) {
        super(0.25F, props);
        this.typeSupplier = typeSupplier;
        this.factory = factory;
        this.connectCap = connectCap;
        this.kind = kind;
        BlockState def = stateDefinition.any();
        for (BooleanProperty p : PROPERTY_BY_DIRECTION.values()) {
            def = def.setValue(p, false);
        }
        registerDefaultState(def);
    }

    public String getKind() {
        return kind;
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
        Block neighborBlock = level.getBlockState(neighborPos).getBlock();
        if (neighborBlock instanceof BlockPipe<?> otherPipe) {
            return otherPipe.kind.equals(kind);
        }
        BlockEntity be = level.getBlockEntity(neighborPos);
        return be != null && be.getCapability(connectCap.get(), dir.getOpposite()).isPresent();
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MenuProvider provider) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, provider, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TilePipeDiamond diamond) {
                diamond.dropContents(level, pos);
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
