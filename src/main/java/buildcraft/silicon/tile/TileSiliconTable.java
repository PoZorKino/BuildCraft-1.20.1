/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;

/** Base for the silicon tables (Integration/Programming): energy-powered item transformers. */
public abstract class TileSiliconTable extends BlockEntity implements MenuProvider, ITickingMachine {

    public static final int ENERGY_CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int WORK_RATE = 400;

    protected final int inputCount;
    protected final ItemStackHandler inv;
    protected final MachineEnergyStorage energy = new MachineEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE);

    private final IItemHandler automation;
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private final LazyOptional<IItemHandler> itemCap;

    private int progress;
    private int cost = 1;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                case 2 -> progress;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> energy.setEnergyStored(value);
                case 2 -> progress = value;
                default -> { }
            }
        }
        @Override public int getCount() { return 3; }
    };

    protected TileSiliconTable(BlockEntityType<?> type, BlockPos pos, BlockState state, int inputCount) {
        super(type, pos, state);
        this.inputCount = inputCount;
        this.inv = new ItemStackHandler(inputCount + 1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        int outputSlot = inputCount;
        this.automation = new IItemHandler() {
            @Override public int getSlots() { return inv.getSlots(); }
            @Nonnull @Override public ItemStack getStackInSlot(int slot) { return inv.getStackInSlot(slot); }
            @Nonnull @Override public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return slot < outputSlot ? inv.insertItem(slot, stack, simulate) : stack;
            }
            @Nonnull @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return slot == outputSlot ? inv.extractItem(outputSlot, amount, simulate) : ItemStack.EMPTY;
            }
            @Override public int getSlotLimit(int slot) { return inv.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return slot < outputSlot; }
        };
        this.itemCap = LazyOptional.of(() -> automation);
    }

    public int getInputCount() {
        return inputCount;
    }

    public ItemStackHandler getInventory() {
        return inv;
    }

    public ContainerData getData() {
        return data;
    }

    /** @return the crafted result for the current inputs, or empty if the inputs do not form a recipe. */
    protected abstract ItemStack getResult();

    /** Energy required for the current recipe. */
    protected abstract int getCost();

    /** Consume one of each input used by the recipe. */
    protected abstract void consumeInputs();

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        ItemStack result = getResult();
        if (result.isEmpty()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }
        cost = Math.max(1, getCost());
        ItemStack out = inv.getStackInSlot(inputCount);
        boolean canOutput = out.isEmpty()
                || (ItemStack.isSameItemSameTags(out, result) && out.getCount() + result.getCount() <= out.getMaxStackSize());
        if (!canOutput) {
            return;
        }
        int spend = Math.min(WORK_RATE, Math.min(energy.getEnergyStored(), cost - progress));
        if (spend > 0 && energy.spend(spend)) {
            progress += spend;
            setChanged();
        }
        if (progress >= cost) {
            progress = 0;
            consumeInputs();
            if (out.isEmpty()) {
                inv.setStackInSlot(inputCount, result);
            } else {
                out.grow(result.getCount());
            }
            setChanged();
        }
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
}
