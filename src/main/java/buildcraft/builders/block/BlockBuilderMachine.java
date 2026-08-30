/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.builders.tile.TemplateHolder;
import buildcraft.factory.tile.ITickingMachine;

/**
 * Shared horizontal-facing machine block for the builders module (Architect, Builder, Filler). Ticks
 * its block entity and, for machines with a template slot, loads/unloads the template on right-click.
 */
public class BlockBuilderMachine<T extends BlockEntity> extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final Supplier<BlockEntityType<T>> typeSupplier;
    private final BiFunction<BlockPos, BlockState, T> factory;

    public BlockBuilderMachine(Properties props, Supplier<BlockEntityType<T>> typeSupplier,
            BiFunction<BlockPos, BlockState, T> factory) {
        super(props);
        this.typeSupplier = typeSupplier;
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TemplateHolder holder)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            ItemStack current = holder.getTemplate();
            if (current.isEmpty() && held.is(buildcraft.registry.BCItems.TEMPLATE.get())) {
                holder.setTemplate(held.split(1));
            } else if (!current.isEmpty()) {
                if (!player.getInventory().add(current)) {
                    player.drop(current, false);
                }
                holder.setTemplate(ItemStack.EMPTY);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TemplateHolder holder && !holder.getTemplate().isEmpty()) {
                Block.popResource(level, pos, holder.getTemplate());
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
