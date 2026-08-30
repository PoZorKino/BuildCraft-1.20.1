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
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.energy.block.BlockEngineStone;
import buildcraft.energy.util.EngineEnergyStorage;
import buildcraft.energy.menu.StirlingEngineMenu;
import buildcraft.registry.BCBlockEntities;

/**
 * Server-side logic for the Stirling Engine: burns fuel from its slot to produce energy, and emits
 * that energy from the face it points towards.
 */
public class StirlingEngineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_ENERGY = 10_000;
    /** Energy generated each tick while a fuel item is burning. */
    public static final int GENERATION_RATE = 10;
    /** Maximum energy pushed to the connected machine each tick. */
    public static final int OUTPUT_RATE = 40;

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

    private final EngineEnergyStorage energy = new EngineEnergyStorage(MAX_ENERGY, OUTPUT_RATE);

    private final LazyOptional<IItemHandler> fuelCap = LazyOptional.of(() -> fuel);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);

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
                case 3 -> { /* capacity is constant */ }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public StirlingEngineBlockEntity(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ENGINE_STONE.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean wasBurning = burnTime > 0;
        boolean redstone = level.hasNeighborSignal(pos);

        if (burnTime > 0) {
            burnTime--;
            energy.generate(GENERATION_RATE);
        }

        // Only start consuming fuel while receiving a redstone signal, matching the classic engine.
        if (burnTime <= 0 && redstone) {
            ItemStack stack = fuel.getStackInSlot(0);
            int itemBurn = ForgeHooks.getBurnTime(stack, null);
            if (itemBurn > 0) {
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

        pushEnergy(level, pos, state);

        if (wasBurning != (burnTime > 0)) {
            setChanged();
        }
        // Always sync so the GUI energy/heat bars stay live.
        level.sendBlockUpdated(pos, state, state, 3);
    }

    private void pushEnergy(Level level, BlockPos pos, BlockState state) {
        int stored = energy.getEnergyStored();
        if (stored <= 0) {
            return;
        }
        Direction facing = state.getValue(BlockEngineStone.FACING);
        BlockEntity neighbor = level.getBlockEntity(pos.relative(facing));
        if (neighbor == null) {
            return;
        }
        neighbor.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite()).ifPresent(target -> {
            if (!target.canReceive()) {
                return;
            }
            int toOffer = Math.min(OUTPUT_RATE, energy.getEnergyStored());
            int accepted = target.receiveEnergy(toOffer, false);
            if (accepted > 0) {
                energy.consume(accepted);
                setChanged();
            }
        });
    }

    /** 0..4 mapped from stored-energy ratio, matching BuildCraft power stages (blue..overheat). */
    public int getPowerStage() {
        double ratio = energy.getEnergyStored() / (double) energy.getMaxEnergyStored();
        if (ratio < 0.25) return 0; // blue
        if (ratio < 0.50) return 1; // green
        if (ratio < 0.75) return 2; // yellow
        if (ratio < 1.00) return 3; // red
        return 4;                   // overheat
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, fuel.getStackInSlot(0));
        Containers.dropContents(level, pos, container);
    }

    // --- Capabilities -------------------------------------------------------

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return fuelCap.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelCap.invalidate();
        energyCap.invalidate();
    }

    // --- NBT ----------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fuel", fuel.serializeNBT());
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("burnTime", burnTime);
        tag.putInt("currentItemBurnTime", currentItemBurnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel.deserializeNBT(tag.getCompound("fuel"));
        energy.setEnergyStored(tag.getInt("energy"));
        burnTime = tag.getInt("burnTime");
        currentItemBurnTime = tag.getInt("currentItemBurnTime");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    // --- Menu ---------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.engine_stone");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new StirlingEngineMenu(id, playerInv, fuel, data, getBlockPos());
    }

    public ContainerData getContainerData() {
        return data;
    }
}
