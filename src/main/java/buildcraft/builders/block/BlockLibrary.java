/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.builders.BlueprintData;
import buildcraft.builders.tile.TileLibrary;
import buildcraft.registry.BCItems;

/**
 * Blueprint Library. Right-click with a filled Template to store its blueprint; right-click with a
 * blank Template to stamp a copy of the stored blueprint onto it.
 */
public class BlockLibrary extends Block implements EntityBlock {

    public BlockLibrary(Properties props) {
        super(props);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileLibrary library)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.is(BCItems.TEMPLATE.get()) && BlueprintData.hasData(held)) {
            library.setStored(BlueprintData.copyBlueprint(held));
            player.displayClientMessage(Component.translatable("message.buildcraft.library.stored"), true);
            return InteractionResult.CONSUME;
        }
        if (held.is(BCItems.TEMPLATE.get()) && !BlueprintData.hasData(held) && library.hasStored()) {
            if (held.getCount() == 1) {
                BlueprintData.setBlueprint(held, library.getStored());
            } else {
                held.shrink(1);
                ItemStack copy = new ItemStack(BCItems.TEMPLATE.get());
                BlueprintData.setBlueprint(copy, library.getStored());
                if (!player.getInventory().add(copy)) {
                    player.drop(copy, false);
                }
            }
            player.displayClientMessage(Component.translatable("message.buildcraft.library.copied"), true);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable(
                library.hasStored() ? "message.buildcraft.library.ready" : "message.buildcraft.library.empty"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLibrary(pos, state);
    }
}
