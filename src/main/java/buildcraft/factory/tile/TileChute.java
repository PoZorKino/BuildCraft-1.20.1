/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import buildcraft.registry.BCBlockEntities;

/**
 * Chute: a powered hopper for the pipe network. Vacuums dropped items above it and pulls from an
 * inventory above, then pushes everything into the inventory (or pipe) below.
 */
public class TileChute extends BlockEntity implements ITickingMachine {

    public static final int INTERVAL = 4;

    private final ItemStackHandler buffer = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> buffer);
    private int cooldown;

    public TileChute(BlockPos pos, BlockState state) {
        super(BCBlockEntities.CHUTE.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        pushDown(level, pos);

        if (cooldown > 0) {
            cooldown--;
            return;
        }
        // Vacuum dropped items just above the chute.
        AABB area = new AABB(pos.above()).inflate(0.25, 0.25, 0.25).expandTowards(0, 0.5, 0);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, area, e -> e.isAlive() && !e.getItem().isEmpty() && !e.hasPickUpDelay());
        for (ItemEntity entity : drops) {
            ItemStack leftover = insert(entity.getItem().copy());
            if (leftover.getCount() != entity.getItem().getCount()) {
                if (leftover.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(leftover);
                }
                cooldown = INTERVAL;
                setChanged();
                return;
            }
        }
        // Pull from an inventory above.
        BlockEntity above = level.getBlockEntity(pos.above());
        if (above != null && !(above instanceof TileChute)) {
            IItemHandler src = above.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElse(null);
            if (src != null) {
                for (int slot = 0; slot < src.getSlots(); slot++) {
                    ItemStack extracted = src.extractItem(slot, 4, true);
                    if (!extracted.isEmpty()) {
                        ItemStack leftover = insert(extracted.copy());
                        int moved = extracted.getCount() - leftover.getCount();
                        if (moved > 0) {
                            src.extractItem(slot, moved, false);
                            cooldown = INTERVAL;
                            setChanged();
                            return;
                        }
                    }
                }
            }
        }
    }

    private ItemStack insert(ItemStack stack) {
        for (int slot = 0; slot < buffer.getSlots(); slot++) {
            stack = buffer.insertItem(slot, stack, false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    private void pushDown(Level level, BlockPos pos) {
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below == null || below instanceof TileChute) {
            return;
        }
        IItemHandler dest = below.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElse(null);
        if (dest == null) {
            return;
        }
        for (int slot = 0; slot < buffer.getSlots(); slot++) {
            ItemStack in = buffer.getStackInSlot(slot);
            if (!in.isEmpty()) {
                ItemStack leftover = ItemHandlerHelper.insertItem(dest, in, false);
                if (leftover.getCount() != in.getCount()) {
                    buffer.setStackInSlot(slot, leftover);
                    setChanged();
                }
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer c = new SimpleContainer(buffer.getSlots());
        for (int i = 0; i < buffer.getSlots(); i++) {
            c.setItem(i, buffer.getStackInSlot(i));
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
        tag.put("buffer", buffer.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        buffer.deserializeNBT(tag.getCompound("buffer"));
    }
}
