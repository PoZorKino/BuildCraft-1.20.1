/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import buildcraft.registry.BCMenuTypes;

/** Container menu for the Stirling Engine GUI (1 fuel slot + the player inventory). */
public class StirlingEngineMenu extends AbstractContainerMenu {

    private final ContainerData data;
    public final BlockPos pos;

    /** Client-side constructor used by the menu type factory. */
    public StirlingEngineMenu(int id, Inventory playerInv, net.minecraft.network.FriendlyByteBuf buf) {
        this(id, playerInv, new ItemStackHandler(1), new SimpleContainerData(4), buf.readBlockPos());
    }

    public StirlingEngineMenu(int id, Inventory playerInv, IItemHandler fuel, ContainerData data, BlockPos pos) {
        super(BCMenuTypes.ENGINE_STONE.get(), id);
        this.data = data;
        this.pos = pos;

        addSlot(new SlotItemHandler(fuel, 0, 80, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
        });

        // Player inventory.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Player hotbar.
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public int getBurnTime() {
        return data.get(0);
    }

    public int getCurrentItemBurnTime() {
        return data.get(1);
    }

    public int getEnergy() {
        return data.get(2);
    }

    public int getMaxEnergy() {
        int max = data.get(3);
        return max <= 0 ? 1 : max;
    }

    /** Scaled 0..height flame overlay for the burning fuel indicator. */
    public int getBurnScaled(int height) {
        int total = getCurrentItemBurnTime();
        if (total <= 0) {
            return 0;
        }
        return getBurnTime() * height / total;
    }

    /** Scaled 0..height energy bar. */
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
            int invStart = 1;
            int invEnd = slots.size();
            if (index < invStart) {
                // From engine fuel slot into the player inventory.
                if (!moveItemStackTo(stack, invStart, invEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory into the fuel slot (only if it burns).
                if (ForgeHooks.getBurnTime(stack, null) > 0) {
                    if (!moveItemStackTo(stack, 0, invStart, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
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
