/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.block;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;

import buildcraft.factory.tile.TileRefinery;

/** The Refinery block: bucket-interactable, and reports its contents/energy on right-click. */
public class BlockRefinery extends BlockMachine<TileRefinery> {

    public BlockRefinery(Properties props, Supplier<BlockEntityType<TileRefinery>> typeSupplier,
            BiFunction<BlockPos, BlockState, TileRefinery> factory) {
        super(props, typeSupplier, factory);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player.getItemInHand(hand).isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileRefinery refinery) {
                int oil = refinery.getOil().getAmount();
                int fuel = refinery.getFuel().getAmount();
                player.displayClientMessage(Component.translatable("message.buildcraft.refinery.status",
                        oil, fuel, refinery.getEnergyStored()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
