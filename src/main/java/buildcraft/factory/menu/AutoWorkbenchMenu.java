/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.menu;

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

/** Menu for the Auto Workbench: a 3x3 grid, an output slot, and the player inventory. */
public class AutoWorkbenchMenu extends AbstractContainerMenu {

    public final BlockPos pos;

    public AutoWorkbenchMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, new ItemStackHandler(10), buf.readBlockPos());
    }

    public AutoWorkbenchMenu(int id, Inventory playerInv, IItemHandler inv, BlockPos pos) {
        super(BCMenuTypes.AUTO_WORKBENCH.get(), id);
        this.pos = pos;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new SlotItemHandler(inv, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }
        // Output slot.
        addSlot(new SlotItemHandler(inv, 9, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int invStart = 10;
            if (index < invStart) {
                if (!moveItemStackTo(stack, invStart, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 9, false)) {
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
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, BCBlocks.AUTO_WORKBENCH.get());
    }
}
