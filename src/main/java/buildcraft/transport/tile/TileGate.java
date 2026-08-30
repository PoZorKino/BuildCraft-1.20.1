/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockGate;

/**
 * Gate: watches the machine it faces and emits a redstone signal when a selected condition holds.
 * A functional distillation of BuildCraft's trigger→action gates (the full wire/gate network and
 * pipe-attachment framework are not part of this port).
 */
public class TileGate extends BlockEntity implements ITickingMachine {

    public enum Trigger {
        ITEMS_PRESENT, ITEMS_FULL, FLUID_PRESENT, FLUID_FULL, ENERGY_PRESENT, ENERGY_FULL, ALWAYS
    }

    private Trigger trigger = Trigger.ITEMS_PRESENT;

    public TileGate(BlockPos pos, BlockState state) {
        super(BCBlockEntities.GATE.get(), pos, state);
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Trigger cycleTrigger() {
        Trigger[] values = Trigger.values();
        trigger = values[(trigger.ordinal() + 1) % values.length];
        setChanged();
        return trigger;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean powered = evaluate(level, pos, state);
        if (state.getValue(BlockGate.POWERED) != powered) {
            level.setBlock(pos, state.setValue(BlockGate.POWERED, powered), 3);
            level.updateNeighborsAt(pos, state.getBlock());
        }
    }

    private boolean evaluate(Level level, BlockPos pos, BlockState state) {
        if (trigger == Trigger.ALWAYS) {
            return true;
        }
        Direction facing = state.getValue(BlockGate.FACING);
        BlockEntity target = level.getBlockEntity(pos.relative(facing));
        if (target == null) {
            return false;
        }
        Direction from = facing.getOpposite();
        return switch (trigger) {
            case ITEMS_PRESENT -> {
                IItemHandler h = target.getCapability(ForgeCapabilities.ITEM_HANDLER, from).orElse(null);
                yield h != null && anyItem(h);
            }
            case ITEMS_FULL -> {
                IItemHandler h = target.getCapability(ForgeCapabilities.ITEM_HANDLER, from).orElse(null);
                yield h != null && isFull(h);
            }
            case FLUID_PRESENT -> {
                IFluidHandler h = target.getCapability(ForgeCapabilities.FLUID_HANDLER, from).orElse(null);
                yield h != null && fluidAmount(h) > 0;
            }
            case FLUID_FULL -> {
                IFluidHandler h = target.getCapability(ForgeCapabilities.FLUID_HANDLER, from).orElse(null);
                yield h != null && fluidFull(h);
            }
            case ENERGY_PRESENT -> {
                IEnergyStorage e = target.getCapability(ForgeCapabilities.ENERGY, from).orElse(null);
                yield e != null && e.getEnergyStored() > 0;
            }
            case ENERGY_FULL -> {
                IEnergyStorage e = target.getCapability(ForgeCapabilities.ENERGY, from).orElse(null);
                yield e != null && e.getEnergyStored() >= e.getMaxEnergyStored() && e.getMaxEnergyStored() > 0;
            }
            default -> false;
        };
    }

    private static boolean anyItem(IItemHandler h) {
        for (int i = 0; i < h.getSlots(); i++) {
            if (!h.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFull(IItemHandler h) {
        for (int i = 0; i < h.getSlots(); i++) {
            var stack = h.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < Math.min(stack.getMaxStackSize(), h.getSlotLimit(i))) {
                return false;
            }
        }
        return h.getSlots() > 0;
    }

    private static int fluidAmount(IFluidHandler h) {
        int total = 0;
        for (int i = 0; i < h.getTanks(); i++) {
            total += h.getFluidInTank(i).getAmount();
        }
        return total;
    }

    private static boolean fluidFull(IFluidHandler h) {
        for (int i = 0; i < h.getTanks(); i++) {
            if (h.getFluidInTank(i).getAmount() < h.getTankCapacity(i)) {
                return false;
            }
        }
        return h.getTanks() > 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("trigger", trigger.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Trigger[] values = Trigger.values();
        trigger = values[Math.floorMod(tag.getInt("trigger"), values.length)];
    }
}
