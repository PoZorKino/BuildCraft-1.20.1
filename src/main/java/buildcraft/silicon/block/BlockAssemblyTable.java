/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import buildcraft.factory.block.BlockMachine;
import buildcraft.silicon.tile.TileAssemblyTable;

/** The Assembly Table block: opens its GUI on right-click and drops its inventory when broken. */
public class BlockAssemblyTable extends BlockMachine<TileAssemblyTable> {

    public BlockAssemblyTable(Properties props, Supplier<BlockEntityType<TileAssemblyTable>> typeSupplier,
            BiFunction<BlockPos, BlockState, TileAssemblyTable> factory) {
        super(props, typeSupplier, factory);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider provider && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, provider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileAssemblyTable table) {
                table.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, moving);
        }
    }
}
