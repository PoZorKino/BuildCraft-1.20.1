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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;
import buildcraft.registry.BCItems;
import buildcraft.registry.BCMenuTypes;
import buildcraft.silicon.menu.SiliconTableMenu;

/** Integration Table: upgrades a robot board by integrating it with a chipset. */
public class TileIntegrationTable extends TileSiliconTable {

    public TileIntegrationTable(BlockPos pos, BlockState state) {
        super(BCBlockEntities.INTEGRATION_TABLE.get(), pos, state, 2);
    }

    @Override
    protected ItemStack getResult() {
        Item a = inv.getStackInSlot(0).getItem();
        Item b = inv.getStackInSlot(1).getItem();
        if (matches(a, b, BCItems.BOARD_RED.get(), BCItems.CHIPSET_GOLD.get())) {
            return new ItemStack(BCItems.BOARD_GREEN.get());
        }
        if (matches(a, b, BCItems.BOARD_GREEN.get(), BCItems.CHIPSET_DIAMOND.get())) {
            return new ItemStack(BCItems.BOARD_BLUE.get());
        }
        return ItemStack.EMPTY;
    }

    private static boolean matches(Item a, Item b, Item first, Item second) {
        return (a == first && b == second) || (a == second && b == first);
    }

    @Override
    protected int getCost() {
        return 4_000;
    }

    @Override
    protected void consumeInputs() {
        inv.extractItem(0, 1, false);
        inv.extractItem(1, 1, false);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.integration_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new SiliconTableMenu(BCMenuTypes.INTEGRATION_TABLE.get(), id, playerInv, inv, getData(),
                getBlockPos(), 2);
    }
}
