/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blueprint storage for the Architect/Builder/Library. Captures the full {@link BlockState} of every
 * block in a cuboid into a template {@link net.minecraft.world.item.ItemStack}'s NBT (preserving
 * orientation and other properties), and rebuilds them elsewhere.
 */
public final class BlueprintData {

    public static final int SIZE = 5;
    private static final String KEY = "blueprint";

    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    private final List<BlockState> palette;
    public final int[] blocks;

    private BlueprintData(int sizeX, int sizeY, int sizeZ, List<BlockState> palette, int[] blocks) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palette = palette;
        this.blocks = blocks;
    }

    public int volume() {
        return sizeX * sizeY * sizeZ;
    }

    public static boolean hasData(net.minecraft.world.item.ItemStack template) {
        return template.hasTag() && template.getTag().contains(KEY);
    }

    /** @return a detached copy of the blueprint NBT stored on the template. */
    public static CompoundTag copyBlueprint(net.minecraft.world.item.ItemStack template) {
        return template.getTag().getCompound(KEY).copy();
    }

    /** Stamp the given blueprint NBT onto the template. */
    public static void setBlueprint(net.minecraft.world.item.ItemStack template, CompoundTag blueprint) {
        template.getOrCreateTag().put(KEY, blueprint.copy());
    }

    /** Scan a cuboid whose corner is {@code origin}, extending {@code SIZE} in each axis. */
    public static void scan(Level level, BlockPos origin, net.minecraft.world.item.ItemStack template) {
        List<BlockState> palette = new ArrayList<>();
        int[] blocks = new int[SIZE * SIZE * SIZE];
        int i = 0;
        for (int dx = 0; dx < SIZE; dx++) {
            for (int dy = 0; dy < SIZE; dy++) {
                for (int dz = 0; dz < SIZE; dz++) {
                    BlockState state = level.getBlockState(origin.offset(dx, dy, dz));
                    int idx = palette.indexOf(state);
                    if (idx < 0) {
                        idx = palette.size();
                        palette.add(state);
                    }
                    blocks[i++] = idx;
                }
            }
        }
        CompoundTag bp = new CompoundTag();
        bp.putInt("sx", SIZE);
        bp.putInt("sy", SIZE);
        bp.putInt("sz", SIZE);
        ListTag paletteTag = new ListTag();
        for (BlockState state : palette) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        bp.put("palette", paletteTag);
        bp.putIntArray("blocks", blocks);
        template.getOrCreateTag().put(KEY, bp);
    }

    public static BlueprintData read(net.minecraft.world.item.ItemStack template) {
        CompoundTag bp = template.getTag().getCompound(KEY);
        ListTag paletteTag = bp.getList("palette", 10);
        List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), paletteTag.getCompound(i)));
        }
        return new BlueprintData(bp.getInt("sx"), bp.getInt("sy"), bp.getInt("sz"), palette, bp.getIntArray("blocks"));
    }

    /** @return the block state at flat index {@code i}, or null for air. */
    public BlockState stateAt(int i) {
        if (i < 0 || i >= blocks.length) {
            return null;
        }
        BlockState state = palette.get(blocks[i]);
        return state.isAir() ? null : state;
    }

    /** Convert a flat index to the relative offset from the build origin. */
    public BlockPos offsetFor(int i) {
        int dz = i % sizeZ;
        int dy = (i / sizeZ) % sizeY;
        int dx = i / (sizeZ * sizeY);
        return new BlockPos(dx, dy, dz);
    }

    /** The corner of the region in front of a machine at {@code pos} facing {@code facing}. */
    public static BlockPos regionOrigin(BlockPos pos, Direction facing) {
        BlockPos front = pos.relative(facing);
        return front.offset(-SIZE / 2, 0, -SIZE / 2);
    }
}
