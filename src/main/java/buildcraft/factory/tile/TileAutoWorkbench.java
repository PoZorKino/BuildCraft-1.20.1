/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.tile;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.factory.menu.AutoWorkbenchMenu;
import buildcraft.factory.util.SimpleCraftingContainer;
import buildcraft.registry.BCBlockEntities;

/**
 * Auto Workbench: continuously crafts the recipe formed by its 3x3 grid as long as the grid is
 * stocked, placing results in its output slot. Keep the grid supplied (by hand or pipes) and pull
 * finished items out of the output.
 */
public class TileAutoWorkbench extends BlockEntity implements MenuProvider, ITickingMachine {

    public static final int GRID = 9;
    public static final int OUTPUT = 9; // slot index of the output within the 10-slot handler
    public static final int CRAFT_INTERVAL = 8;

    private final ItemStackHandler inv = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // Automation view: insert into the 3x3 grid, extract from the output slot.
    private final IItemHandler automation = new IItemHandler() {
        @Override public int getSlots() { return 10; }
        @Nonnull @Override public ItemStack getStackInSlot(int slot) { return inv.getStackInSlot(slot); }
        @Nonnull @Override public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot < GRID ? inv.insertItem(slot, stack, simulate) : stack;
        }
        @Nonnull @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == OUTPUT ? inv.extractItem(OUTPUT, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return inv.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return slot < GRID; }
    };

    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> automation);
    private int cooldown;

    public TileAutoWorkbench(BlockPos pos, BlockState state) {
        super(BCBlockEntities.AUTO_WORKBENCH.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        SimpleCraftingContainer crafting = new SimpleCraftingContainer(3, 3);
        for (int i = 0; i < GRID; i++) {
            crafting.setItem(i, inv.getStackInSlot(i));
        }
        Optional<CraftingRecipe> recipeOpt = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level);
        if (recipeOpt.isEmpty()) {
            return;
        }
        CraftingRecipe recipe = recipeOpt.get();
        ItemStack result = recipe.assemble(crafting, level.registryAccess());
        if (result.isEmpty()) {
            return;
        }
        ItemStack output = inv.getStackInSlot(OUTPUT);
        boolean canOutput = output.isEmpty()
                || (ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());
        if (!canOutput) {
            return;
        }
        // Consume one of every ingredient present, honouring container remainders (e.g. buckets).
        var remainders = recipe.getRemainingItems(crafting);
        for (int i = 0; i < GRID; i++) {
            ItemStack slotStack = inv.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
                slotStack.shrink(1);
                inv.setStackInSlot(i, slotStack);
            }
            if (i < remainders.size()) {
                ItemStack rem = remainders.get(i);
                if (!rem.isEmpty()) {
                    if (inv.getStackInSlot(i).isEmpty()) {
                        inv.setStackInSlot(i, rem);
                    } else {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rem);
                    }
                }
            }
        }
        if (output.isEmpty()) {
            inv.setStackInSlot(OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }
        cooldown = CRAFT_INTERVAL;
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer c = new SimpleContainer(inv.getSlots());
        for (int i = 0; i < inv.getSlots(); i++) {
            c.setItem(i, inv.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, c);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inv", inv.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inv.deserializeNBT(tag.getCompound("inv"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.auto_workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AutoWorkbenchMenu(id, playerInv, inv, getBlockPos());
    }
}
