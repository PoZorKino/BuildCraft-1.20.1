/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.registry.BCBlockEntities;
import buildcraft.transport.block.BlockPipe;
import buildcraft.transport.menu.DiamondPipeMenu;

/**
 * Diamond transport pipe: a filtered router. Each of the six faces has nine filter slots; items
 * prefer sides whose filters match, fall back to unfiltered connected sides, and never take a
 * side whose filters exclude them.
 */
public class TilePipeDiamond extends TilePipe implements MenuProvider {

    public static final int FILTERS_PER_SIDE = 9;
    public static final int FILTER_SLOTS = FILTERS_PER_SIDE * 6;

    private final ItemStackHandler filters = new ItemStackHandler(FILTER_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public TilePipeDiamond(BlockPos pos, BlockState state) {
        super(BCBlockEntities.PIPE_DIAMOND.get(), pos, state, 8);
    }

    public ItemStackHandler getFilters() {
        return filters;
    }

    /**
     * Whether {@code stack} may leave through {@code side}. An empty filter row allows everything;
     * a populated row allows only matching items.
     */
    public boolean allows(Direction side, ItemStack stack) {
        int offset = side.ordinal() * FILTERS_PER_SIDE;
        boolean anyFilter = false;
        for (int i = 0; i < FILTERS_PER_SIDE; i++) {
            ItemStack filter = filters.getStackInSlot(offset + i);
            if (filter.isEmpty()) {
                continue;
            }
            anyFilter = true;
            if (ItemStack.isSameItem(filter, stack)) {
                return true;
            }
        }
        return PipeFilterLogic.allows(anyFilter, false);
    }

    public boolean hasFilters(Direction side) {
        int offset = side.ordinal() * FILTERS_PER_SIDE;
        for (int i = 0; i < FILTERS_PER_SIDE; i++) {
            if (!filters.getStackInSlot(offset + i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<Direction> collectOutputs(BlockState state, TravelingItem ti) {
        List<Direction> matched = new ArrayList<>();
        List<Direction> unfiltered = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (!BlockPipe.isConnected(state, dir) || dir == ti.from) {
                continue;
            }
            if (!allows(dir, ti.stack)) {
                continue;
            }
            if (hasFilters(dir)) {
                matched.add(dir);
            } else {
                unfiltered.add(dir);
            }
        }
        if (!matched.isEmpty()) {
            return matched;
        }
        if (!unfiltered.isEmpty()) {
            return unfiltered;
        }
        if (BlockPipe.isConnected(state, ti.from) && allows(ti.from, ti.stack)) {
            return List.of(ti.from);
        }
        return List.of();
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(FILTER_SLOTS);
        for (int i = 0; i < FILTER_SLOTS; i++) {
            container.setItem(i, filters.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, container);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraft.pipe_diamond");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new DiamondPipeMenu(id, playerInv, filters, getBlockPos());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("filters", filters.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        filters.deserializeNBT(tag.getCompound("filters"));
    }
}
