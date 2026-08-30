/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.DyeColor;
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
import buildcraft.transport.pipe.PipeAttachment;

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
    private final EnumMap<Direction, PipeAttachment> attachments = new EnumMap<>(Direction.class);
    private boolean wirePowered;
    @Nullable
    private DyeColor color;

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
        // Keep clients in sync so the travelling items animate inside the pipe.
        if (!items.isEmpty() || changed) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /** Read-only view of the items currently travelling through this pipe (used by the renderer). */
    public List<TravelingItem> getTravelingItems() {
        return items;
    }

    public int getTransitTicks() {
        return transitTicks;
    }

    @Nullable
    public PipeAttachment getAttachment(Direction side) {
        return attachments.get(side);
    }

    public Map<Direction, PipeAttachment> getAttachments() {
        return attachments;
    }

    public boolean isSideBlocked(Direction side) {
        PipeAttachment attachment = attachments.get(side);
        return attachment != null && attachment.isBlocking();
    }

    public boolean hasWire() {
        for (PipeAttachment attachment : attachments.values()) {
            if (attachment.kind == PipeAttachment.Kind.WIRE) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWire(DyeColor color) {
        for (PipeAttachment attachment : attachments.values()) {
            if (attachment.kind == PipeAttachment.Kind.WIRE && attachment.color == color) {
                return true;
            }
        }
        return false;
    }

    public boolean isWirePowered() {
        return wirePowered;
    }

    @Nullable
    public DyeColor getColor() {
        return color;
    }

    public void setColor(@Nullable DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockPipe.refreshConnections(level, worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** Attach to {@code side} if the face is free. Refreshes pipe connections and wire power. */
    public boolean attach(Direction side, PipeAttachment attachment) {
        if (attachments.containsKey(side)) {
            return false;
        }
        attachments.put(side, attachment);
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockPipe.refreshConnections(level, worldPosition);
            updateWirePower();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    @Nullable
    public PipeAttachment removeAttachment(Direction side) {
        PipeAttachment removed = attachments.remove(side);
        if (removed != null) {
            setChanged();
            if (level != null && !level.isClientSide) {
                BlockPipe.refreshConnections(level, worldPosition);
                updateWirePower();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return removed;
    }

    public void dropAttachments(Level level, BlockPos pos) {
        for (PipeAttachment attachment : attachments.values()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    attachment.asItem());
        }
        attachments.clear();
    }

    public void updateWirePower() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean powered = computeWirePower();
        if (powered != wirePowered) {
            wirePowered = powered;
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            for (Direction dir : Direction.values()) {
                level.updateNeighborsAt(worldPosition.relative(dir), getBlockState().getBlock());
            }
        }
    }

    private boolean computeWirePower() {
        Set<DyeColor> colors = new HashSet<>();
        for (PipeAttachment attachment : attachments.values()) {
            if (attachment.kind == PipeAttachment.Kind.WIRE && attachment.color != null) {
                colors.add(attachment.color);
            }
        }
        if (colors.isEmpty()) {
            return false;
        }
        for (DyeColor color : colors) {
            if (isWireNetworkPowered(color)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWireNetworkPowered(DyeColor color) {
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> queue = new ArrayList<>();
        queue.add(worldPosition);
        visited.add(worldPosition);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove(queue.size() - 1);
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof BlockPipe) {
                    BlockEntity be = level.getBlockEntity(neighborPos);
                    if (be instanceof TilePipe pipe && pipe.hasWire(color) && visited.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                    continue;
                }
                if (level.getSignal(neighborPos, dir) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Candidate exit faces, in preference order. Default: every connected face except the entry,
     * then bounce back as a last resort. Subclasses (iron / diamond) override this to restrict routing.
     */
    protected List<Direction> collectOutputs(BlockState state, TravelingItem ti) {
        List<Direction> outputs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (BlockPipe.isConnected(state, dir) && dir != ti.from) {
                outputs.add(dir);
            }
        }
        if (BlockPipe.isConnected(state, ti.from)) {
            outputs.add(ti.from);
        }
        return outputs;
    }

    private boolean tryExit(Level level, BlockPos pos, BlockState state, TravelingItem ti) {
        for (Direction dir : collectOutputs(state, ti)) {
            if (trySend(level, pos, dir, ti) && ti.stack.isEmpty()) {
                return true;
            }
        }
        return ti.stack.isEmpty();
    }

    /** Attempt to hand {@code ti} to the neighbour in {@code dir}. Updates the leftover stack. */
    protected boolean trySend(Level level, BlockPos pos, Direction dir, TravelingItem ti) {
        BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
        if (neighbor instanceof TilePipe pipe) {
            pipe.accept(ti.stack, dir.getOpposite());
            ti.stack = ItemStack.EMPTY;
            return true;
        }
        if (neighbor != null) {
            IItemHandler handler = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            if (handler != null) {
                ItemStack leftover = ItemHandlerHelper.insertItem(handler, ti.stack, false);
                if (leftover.getCount() != ti.stack.getCount()) {
                    ti.stack = leftover;
                    return leftover.isEmpty();
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
            if (isSideBlocked(side)) {
                return LazyOptional.empty();
            }
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
        ListTag attached = new ListTag();
        for (Map.Entry<Direction, PipeAttachment> entry : attachments.entrySet()) {
            CompoundTag entryTag = entry.getValue().save();
            entryTag.put("side", buildcraft.lib.nbt.NBTUtilBC.writeDirection(entry.getKey()));
            attached.add(entryTag);
        }
        tag.put("attachments", attached);
        tag.putBoolean("wirePowered", wirePowered);
        if (color != null) {
            tag.putString("color", color.getName());
        }
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
        attachments.clear();
        ListTag attached = tag.getList("attachments", 10);
        for (int i = 0; i < attached.size(); i++) {
            CompoundTag entry = attached.getCompound(i);
            Direction side = buildcraft.lib.nbt.NBTUtilBC.readDirection(entry.get("side"));
            PipeAttachment attachment = PipeAttachment.load(entry);
            if (side != null && attachment != null) {
                attachments.put(side, attachment);
            }
        }
        wirePowered = tag.getBoolean("wirePowered");
        color = tag.contains("color") ? DyeColor.byName(tag.getString("color"), null) : null;
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
