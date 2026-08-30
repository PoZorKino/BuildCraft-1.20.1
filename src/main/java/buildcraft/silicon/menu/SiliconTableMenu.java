/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import buildcraft.registry.BCMenuTypes;

/**
 * Shared menu for the silicon "tables" (Integration and Programming): {@code inputCount} input slots
 * plus one output slot, backed by an energy-progress {@link ContainerData}.
 */
public class SiliconTableMenu extends AbstractContainerMenu {

    private final ContainerData data;
    private final int inputCount;
    public final BlockPos pos;

    public static SiliconTableMenu integration(int id, Inventory inv, FriendlyByteBuf buf) {
        return new SiliconTableMenu(BCMenuTypes.INTEGRATION_TABLE.get(), id, inv,
                new ItemStackHandler(3), new SimpleContainerData(3), buf.readBlockPos(), 2);
    }

    public static SiliconTableMenu programming(int id, Inventory inv, FriendlyByteBuf buf) {
        return new SiliconTableMenu(BCMenuTypes.PROGRAMMING_TABLE.get(), id, inv,
                new ItemStackHandler(2), new SimpleContainerData(3), buf.readBlockPos(), 1);
    }

    public SiliconTableMenu(MenuType<?> type, int id, Inventory playerInv, IItemHandler inv,
            ContainerData data, BlockPos pos, int inputCount) {
        super(type, id);
        this.data = data;
        this.pos = pos;
        this.inputCount = inputCount;

        if (inputCount == 1) {
            addSlot(new SlotItemHandler(inv, 0, 44, 35));
        } else {
            addSlot(new SlotItemHandler(inv, 0, 35, 35));
            addSlot(new SlotItemHandler(inv, 1, 53, 35));
        }
        addSlot(new SlotItemHandler(inv, inputCount, 116, 35) {
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

        addDataSlots(data);
    }

    public int getInputCount() {
        return inputCount;
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        int max = data.get(1);
        return max <= 0 ? 1 : max;
    }

    public int getEnergyScaled(int height) {
        return getEnergy() * height / getMaxEnergy();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int invStart = inputCount + 1;
            if (index < invStart) {
                if (!moveItemStackTo(stack, invStart, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, inputCount, false)) {
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
        return true;
    }
}
