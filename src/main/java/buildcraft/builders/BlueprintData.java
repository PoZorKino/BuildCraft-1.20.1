/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Simplified blueprint storage for the Architect/Builder. Captures the block types (by registry id)
 * in a cuboid into a template {@link ItemStack}'s NBT, and can rebuild them elsewhere. Block
 * properties are not preserved (each stored block is rebuilt from its default state).
 */
public final class BlueprintData {

    public static final int SIZE = 5;
    private static final String KEY = "blueprint";

    public final int sizeX;
    public final int sizeY;
    public final int sizeZ;
    public final List<String> palette;
    public final int[] blocks;

    private BlueprintData(int sizeX, int sizeY, int sizeZ, List<String> palette, int[] blocks) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palette = palette;
        this.blocks = blocks;
    }

    public int volume() {
        return sizeX * sizeY * sizeZ;
    }

    public static boolean hasData(ItemStack template) {
        return template.hasTag() && template.getTag().contains(KEY);
    }

    /** Scan a cuboid whose corner is {@code origin}, extending {@code SIZE} in each axis. */
    public static void scan(Level level, BlockPos origin, ItemStack template) {
        Map<String, Integer> paletteIndex = new LinkedHashMap<>();
        List<String> palette = new ArrayList<>();
        int[] blocks = new int[SIZE * SIZE * SIZE];
        int i = 0;
        for (int dx = 0; dx < SIZE; dx++) {
            for (int dy = 0; dy < SIZE; dy++) {
                for (int dz = 0; dz < SIZE; dz++) {
                    BlockState state = level.getBlockState(origin.offset(dx, dy, dz));
                    ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    String name = id == null ? "minecraft:air" : id.toString();
                    Integer idx = paletteIndex.get(name);
                    if (idx == null) {
                        idx = palette.size();
                        paletteIndex.put(name, idx);
                        palette.add(name);
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
        for (String name : palette) {
            paletteTag.add(StringTag.valueOf(name));
        }
        bp.put("palette", paletteTag);
        bp.putIntArray("blocks", blocks);
        template.getOrCreateTag().put(KEY, bp);
    }

    public static BlueprintData read(ItemStack template) {
        CompoundTag bp = template.getTag().getCompound(KEY);
        ListTag paletteTag = bp.getList("palette", 8);
        List<String> palette = new ArrayList<>();
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(paletteTag.getString(i));
        }
        return new BlueprintData(bp.getInt("sx"), bp.getInt("sy"), bp.getInt("sz"), palette, bp.getIntArray("blocks"));
    }

    /** @return the block state at flat index {@code i}, or null for air / unknown blocks. */
    public BlockState stateAt(int i) {
        if (i < 0 || i >= blocks.length) {
            return null;
        }
        String name = palette.get(blocks[i]);
        if (name.equals("minecraft:air")) {
            return null;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
        return block == null ? null : block.defaultBlockState();
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
        // Centre the SIZE x SIZE footprint horizontally and start at the machine's level.
        return front.offset(-SIZE / 2, 0, -SIZE / 2);
    }
}
