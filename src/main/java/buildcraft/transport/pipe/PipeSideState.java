/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.pipe;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.nbt.NBTUtilBC;
import buildcraft.transport.block.BlockPipe;

/**
 * Per-pipe storage for side attachments, paint colour, and coloured-wire redstone. Composed into
 * every pipe tile so item, fluid, and power pipes share the same behaviour.
 */
public final class PipeSideState {

    private final BlockEntity owner;
    private final EnumMap<Direction, PipeAttachment> attachments = new EnumMap<>(Direction.class);
    private boolean wirePowered;
    @Nullable
    private DyeColor color;

    public PipeSideState(BlockEntity owner) {
        this.owner = owner;
    }

    @Nullable
    public PipeAttachment get(Direction side) {
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

    public boolean setColor(@Nullable DyeColor color) {
        if (this.color == color) {
            return false;
        }
        this.color = color;
        notifyChange(false);
        return true;
    }

    public boolean attach(Direction side, PipeAttachment attachment) {
        if (attachments.containsKey(side)) {
            return false;
        }
        attachments.put(side, attachment);
        notifyChange(true);
        return true;
    }

    @Nullable
    public PipeAttachment remove(Direction side) {
        PipeAttachment removed = attachments.remove(side);
        if (removed != null) {
            notifyChange(true);
        }
        return removed;
    }

    public void dropAll(Level level, BlockPos pos) {
        for (PipeAttachment attachment : attachments.values()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    attachment.asItem());
        }
        attachments.clear();
    }

    public void updateWirePower() {
        Level level = owner.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        boolean powered = computeWirePower();
        if (powered != wirePowered) {
            wirePowered = powered;
            owner.setChanged();
            BlockState state = owner.getBlockState();
            BlockPos pos = owner.getBlockPos();
            level.updateNeighborsAt(pos, state.getBlock());
            for (Direction dir : Direction.values()) {
                level.updateNeighborsAt(pos.relative(dir), state.getBlock());
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
        for (DyeColor dye : colors) {
            if (isWireNetworkPowered(dye)) {
                return true;
            }
        }
        return false;
    }

    private boolean isWireNetworkPowered(DyeColor color) {
        Level level = owner.getLevel();
        if (level == null) {
            return false;
        }
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> queue = new ArrayList<>();
        queue.add(owner.getBlockPos());
        visited.add(owner.getBlockPos());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove(queue.size() - 1);
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof BlockPipe) {
                    BlockEntity be = level.getBlockEntity(neighborPos);
                    if (be instanceof IPipeHolder holder && holder.hasWire(color) && visited.add(neighborPos)) {
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

    private void notifyChange(boolean wires) {
        owner.setChanged();
        Level level = owner.getLevel();
        if (level != null && !level.isClientSide) {
            BlockPos pos = owner.getBlockPos();
            BlockPipe.refreshConnections(level, pos);
            if (wires) {
                updateWirePower();
            }
            BlockState state = owner.getBlockState();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public void save(CompoundTag tag) {
        ListTag attached = new ListTag();
        for (Map.Entry<Direction, PipeAttachment> entry : attachments.entrySet()) {
            CompoundTag entryTag = entry.getValue().save();
            entryTag.put("side", NBTUtilBC.writeDirection(entry.getKey()));
            attached.add(entryTag);
        }
        tag.put("attachments", attached);
        tag.putBoolean("wirePowered", wirePowered);
        if (color != null) {
            tag.putString("color", color.getName());
        }
    }

    public void load(CompoundTag tag) {
        attachments.clear();
        ListTag attached = tag.getList("attachments", 10);
        for (int i = 0; i < attached.size(); i++) {
            CompoundTag entry = attached.getCompound(i);
            Direction side = NBTUtilBC.readDirection(entry.get("side"));
            PipeAttachment attachment = PipeAttachment.load(entry);
            if (side != null && attachment != null) {
                attachments.put(side, attachment);
            }
        }
        wirePowered = tag.getBoolean("wirePowered");
        color = tag.contains("color") ? DyeColor.byName(tag.getString("color"), null) : null;
    }
}
