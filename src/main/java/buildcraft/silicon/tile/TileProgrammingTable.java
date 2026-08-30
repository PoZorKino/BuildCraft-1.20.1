/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.tile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;
import buildcraft.registry.BCItems;
import buildcraft.registry.BCMenuTypes;
import buildcraft.silicon.menu.SiliconTableMenu;

/** Programming Table: programs a blank Redstone Board into a working robot board. */
public class TileProgrammingTable extends TileSiliconTable {

    public TileProgrammingTable(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PROGRAMMING_TABLE.get(), pos, state, 1);
    }

    @Override
    protected ItemStack getResult() {
        if (inv.getStackInSlot(0).is(BCItems.BOARD_BLANK.get())) {
            return new ItemStack(BCItems.BOARD_RED.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected int getCost() {
        return 1_000;
    }

    @Override
    protected void consumeInputs() {
        inv.extractItem(0, 1, false);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.programming_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new SiliconTableMenu(BCMenuTypes.PROGRAMMING_TABLE.get(), id, playerInv, inv, getData(),
                getBlockPos(), 1);
    }
}
