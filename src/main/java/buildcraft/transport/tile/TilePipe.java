/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import buildcraft.factory.tile.ITickingMachine;
import buildcraft.transport.block.BlockPipe;

/**
 * Base transport pipe: carries item stacks along the connected pipe network and finally inserts them
 * into a connected inventory. Items travel for {@link #transitTicks} ticks per pipe segment before
 * choosing an exit.
 */
public class TilePipe extends BlockEntity implements ITickingMachine {

    /** A single item stack travelling through the pipe. */
    public static class TravelingItem {
        public ItemStack stack;
        public Direction from;
        public int age;

        TravelingItem(ItemStack stack, Direction from, int age) {
            this.stack = stack;
            this.from = from;
            this.age = age;
        }
    }

    protected final int transitTicks;
    protected final List<TravelingItem> items = new ArrayList<>();

    private final Map<Direction, LazyOptional<IItemHandler>> insertCaps = new EnumMap<>(Direction.class);

    public TilePipe(BlockEntityType<?> type, BlockPos pos, BlockState state, int transitTicks) {
        super(type, pos, state);
        this.transitTicks = transitTicks;
        for (Direction d : Direction.values()) {
            insertCaps.put(d, LazyOptional.of(() -> new PipeInsertHandler(d)));
        }
    }

    /** Queue a stack entering this pipe from {@code from}. */
    public void accept(ItemStack stack, Direction from) {
        if (!stack.isEmpty()) {
            items.add(new TravelingItem(stack.copy(), from, 0));
            setChanged();
        }
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (items.isEmpty()) {
            return;
        }
        Iterator<TravelingItem> it = items.iterator();
        List<TravelingItem> reAdd = new ArrayList<>();
        boolean changed = false;
        while (it.hasNext()) {
            TravelingItem ti = it.next();
            ti.age++;
            if (ti.age >= transitTicks) {
                if (tryExit(level, pos, state, ti)) {
                    it.remove();
                    changed = true;
                } else {
                    // Hold at the exit point and retry next tick.
                    ti.age = transitTicks;
                }
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean tryExit(Level level, BlockPos pos, BlockState state, TravelingItem ti) {
        List<Direction> outputs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (BlockPipe.isConnected(state, dir) && dir != ti.from) {
                outputs.add(dir);
            }
        }
        // Prefer forward directions, but allow bouncing back if nothing else works.
        if (BlockPipe.isConnected(state, ti.from)) {
            outputs.add(ti.from);
        }
        for (Direction dir : outputs) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor instanceof TilePipe pipe) {
                pipe.accept(ti.stack, dir.getOpposite());
                return true;
            }
            if (neighbor != null) {
                IItemHandler handler = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
                if (handler != null) {
                    ItemStack leftover = ItemHandlerHelper.insertItem(handler, ti.stack, false);
                    if (leftover.getCount() != ti.stack.getCount()) {
                        ti.stack = leftover;
                        if (leftover.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // --- Capabilities -------------------------------------------------------

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return insertCaps.get(side).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        insertCaps.values().forEach(LazyOptional::invalidate);
    }

    // --- NBT ----------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (TravelingItem ti : items) {
            CompoundTag entry = new CompoundTag();
            entry.put("stack", ti.stack.save(new CompoundTag()));
            entry.put("from", buildcraft.lib.nbt.NBTUtilBC.writeDirection(ti.from));
            entry.putInt("age", ti.age);
            list.add(entry);
        }
        tag.put("items", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ListTag list = tag.getList("items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack stack = ItemStack.of(entry.getCompound("stack"));
            Direction from = buildcraft.lib.nbt.NBTUtilBC.readDirection(entry.get("from"));
            if (from == null) {
                from = Direction.DOWN;
            }
            items.add(new TravelingItem(stack, from, entry.getInt("age")));
        }
    }

    /** Per-side handler that turns external insertions into travelling items. */
    private class PipeInsertHandler implements IItemHandler {
        private final Direction side;

        PipeInsertHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                accept(stack, side);
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }
}
