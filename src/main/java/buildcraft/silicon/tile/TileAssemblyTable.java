/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;
import buildcraft.registry.BCItems;
import buildcraft.silicon.menu.AssemblyMenu;

/**
 * Assembly Table: consumes energy to laser-etch redstone chipsets from raw materials. Feed it power
 * (via kinesis pipes / a Laser) and an input material, and it produces the matching chipset.
 */
public class TileAssemblyTable extends BlockEntity implements MenuProvider, ITickingMachine {

    public static final int ENERGY_CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int WORK_RATE = 500;

    /** input item -> (output chipset, energy cost). */
    private record Recipe(Item input, Supplier<ItemStack> output, int cost) {}

    private static final List<Recipe> RECIPES = new ArrayList<>();

    private static List<Recipe> recipes() {
        if (RECIPES.isEmpty()) {
            RECIPES.add(new Recipe(Items.REDSTONE, () -> new ItemStack(BCItems.CHIPSET_REDSTONE.get()), 1_000));
            RECIPES.add(new Recipe(Items.IRON_INGOT, () -> new ItemStack(BCItems.CHIPSET_IRON.get()), 2_000));
            RECIPES.add(new Recipe(Items.GOLD_INGOT, () -> new ItemStack(BCItems.CHIPSET_GOLD.get()), 4_000));
            RECIPES.add(new Recipe(Items.DIAMOND, () -> new ItemStack(BCItems.CHIPSET_DIAMOND.get()), 8_000));
            RECIPES.add(new Recipe(Items.QUARTZ, () -> new ItemStack(BCItems.CHIPSET_QUARTZ.get()), 4_000));
        }
        return RECIPES;
    }

    @Nullable
    private static Recipe findRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        for (Recipe r : recipes()) {
            if (input.is(r.input)) {
                return r;
            }
        }
        return null;
    }

    private final ItemStackHandler inv = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 && findRecipe(stack) != null;
        }
    };

    private final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE);

    private final IItemHandler automationHandler = new IItemHandler() {
        @Override public int getSlots() { return 2; }
        @Nonnull @Override public ItemStack getStackInSlot(int slot) { return inv.getStackInSlot(slot); }
        @Nonnull @Override public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return slot == 0 ? inv.insertItem(0, stack, simulate) : stack;
        }
        @Nonnull @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? inv.extractItem(1, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return inv.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return inv.isItemValid(slot, stack); }
    };

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> automationHandler);

    private int progress;
    private int cost = 1;
    @Nullable
    private Recipe lastRecipe;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                case 2 -> progress;
                case 3 -> cost;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> energy.setEnergyStored(value);
                case 2 -> progress = value;
                case 3 -> cost = value;
                default -> { }
            }
        }
        @Override public int getCount() { return 4; }
    };

    public TileAssemblyTable(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ASSEMBLY_TABLE.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        Recipe recipe = findRecipe(inv.getStackInSlot(0));
        if (recipe == null) {
            if (progress != 0) {
                progress = 0;
                lastRecipe = null;
                setChanged();
            }
            return;
        }
        if (recipe != lastRecipe) {
            progress = 0;
            lastRecipe = recipe;
        }
        cost = recipe.cost;
        ItemStack result = recipe.output.get();
        ItemStack out = inv.getStackInSlot(1);
        boolean canOutput = out.isEmpty()
                || (ItemStack.isSameItemSameTags(out, result) && out.getCount() + result.getCount() <= out.getMaxStackSize());
        if (!canOutput) {
            return;
        }
        int toSpend = Math.min(WORK_RATE, Math.min(energy.getEnergyStored(), cost - progress));
        if (toSpend > 0 && energy.spend(toSpend)) {
            progress += toSpend;
            setChanged();
        }
        if (progress >= cost) {
            progress = 0;
            inv.extractItem(0, 1, false);
            if (out.isEmpty()) {
                inv.setStackInSlot(1, result);
            } else {
                out.grow(result.getCount());
            }
            setChanged();
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer c = new SimpleContainer(2);
        c.setItem(0, inv.getStackInSlot(0));
        c.setItem(1, inv.getStackInSlot(1));
        Containers.dropContents(level, pos, c);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
        itemCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inv", inv.serializeNBT());
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inv.deserializeNBT(tag.getCompound("inv"));
        energy.setEnergyStored(tag.getInt("energy"));
        progress = tag.getInt("progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.assembly_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AssemblyMenu(id, playerInv, inv, data, getBlockPos());
    }
}
