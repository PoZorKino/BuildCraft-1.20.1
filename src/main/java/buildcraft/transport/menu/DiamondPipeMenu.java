/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import buildcraft.registry.BCBlocks;
import buildcraft.registry.BCMenuTypes;
import buildcraft.transport.tile.TilePipeDiamond;

/** Six rows of nine filter slots (one row per pipe face) plus the player inventory. */
public class DiamondPipeMenu extends AbstractContainerMenu {

    public static final int FILTER_ROWS = 6;
    public static final int FILTER_COLS = TilePipeDiamond.FILTERS_PER_SIDE;

    public final BlockPos pos;

    public DiamondPipeMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, new ItemStackHandler(TilePipeDiamond.FILTER_SLOTS), buf.readBlockPos());
    }

    public DiamondPipeMenu(int id, Inventory playerInv, IItemHandler filters, BlockPos pos) {
        super(BCMenuTypes.PIPE_DIAMOND.get(), id);
        this.pos = pos;

        for (int row = 0; row < FILTER_ROWS; row++) {
            for (int col = 0; col < FILTER_COLS; col++) {
                addSlot(new SlotItemHandler(filters, col + row * FILTER_COLS, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int filterEnd = TilePipeDiamond.FILTER_SLOTS;
            if (index < filterEnd) {
                if (!moveItemStackTo(stack, filterEnd, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, filterEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, BCBlocks.PIPE_DIAMOND.get());
    }
}
