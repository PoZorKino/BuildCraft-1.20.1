/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.nbt;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;

/**
 * Small NBT helpers ported from BuildCraft's {@code lib.misc.NBTUtilBC}, updated for the 1.20.1 tag
 * classes. Provides null-safe (de)serialisation of a few common value types.
 */
public final class NBTUtilBC {

    private NBTUtilBC() {}

    public static IntArrayTag writeBlockPos(BlockPos pos) {
        return new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @Nullable
    public static BlockPos readBlockPos(@Nullable Tag tag) {
        if (tag instanceof IntArrayTag intArray) {
            int[] data = intArray.getAsIntArray();
            if (data.length == 3) {
                return new BlockPos(data[0], data[1], data[2]);
            }
        } else if (tag instanceof CompoundTag compound
                && compound.contains("x") && compound.contains("y") && compound.contains("z")) {
            return new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        }
        return null;
    }

    public static <E extends Enum<E>> Tag writeEnum(@Nullable E value) {
        return net.minecraft.nbt.StringTag.valueOf(value == null ? "" : value.name());
    }

    @Nullable
    public static <E extends Enum<E>> E readEnum(@Nullable Tag tag, Class<E> clazz) {
        if (tag instanceof net.minecraft.nbt.StringTag) {
            String name = tag.getAsString();
            if (name.isEmpty()) {
                return null;
            }
            try {
                return Enum.valueOf(clazz, name);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    public static Tag writeDirection(@Nullable Direction dir) {
        return writeEnum(dir);
    }

    @Nullable
    public static Direction readDirection(@Nullable Tag tag) {
        return readEnum(tag, Direction.class);
    }
}
