/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.energy.menu.EngineMenu;

/**
 * An engine that burns solid furnace fuel from a single slot (Stirling / Combustion engines). Runs
 * while it holds a redstone signal, consuming fuel to fill its energy buffer.
 */
public abstract class TileEngineFuel extends TileEngineBase implements MenuProvider {

    private final ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
    };

    private final LazyOptional<IItemHandler> fuelCap = LazyOptional.of(() -> fuel);

    private int burnTime;
    private int currentItemBurnTime;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> currentItemBurnTime;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> currentItemBurnTime = value;
                case 2 -> energy.setEnergyStored(value);
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    protected TileEngineFuel(BlockEntityType<?> type, BlockPos pos, BlockState state,
            int capacity, int maxOutput) {
        super(type, pos, state, capacity, maxOutput);
    }

    /** Energy produced each tick while burning. */
    protected abstract int getGenerationRate();

    @Override
    protected void tickEngine(Level level, BlockPos pos, BlockState state) {
        if (burnTime > 0) {
            burnTime--;
            energy.generate(getGenerationRate());
        }

        if (burnTime <= 0 && isRedstonePowered(level, pos)) {
            ItemStack stack = fuel.getStackInSlot(0);
            int itemBurn = ForgeHooks.getBurnTime(stack, null);
            if (itemBurn > 0 && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                burnTime = itemBurn;
                currentItemBurnTime = itemBurn;
                ItemStack container = stack.getCraftingRemainingItem();
                stack.shrink(1);
                if (stack.isEmpty() && !container.isEmpty()) {
                    fuel.setStackInSlot(0, container);
                }
                setChanged();
            }
        }

        if (burnTime <= 0) {
            currentItemBurnTime = 0;
        }
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    protected boolean isActivelyGenerating() {
        return isBurning();
    }

    @Override
    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, fuel.getStackInSlot(0));
        Containers.dropContents(level, pos, container);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return fuelCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fuel", fuel.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putInt("currentItemBurnTime", currentItemBurnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("fuel"));
        burnTime = tag.getInt("burnTime");
        currentItemBurnTime = tag.getInt("currentItemBurnTime");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new EngineMenu(id, playerInv, fuel, data, getBlockPos());
    }
}
