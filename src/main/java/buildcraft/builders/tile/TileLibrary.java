/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.tile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.registry.BCBlockEntities;

/** Blueprint Library: remembers a single blueprint so blank templates can be stamped into copies. */
public class TileLibrary extends BlockEntity {

    @Nullable
    private CompoundTag storedBlueprint;

    public TileLibrary(BlockPos pos, BlockState state) {
        super(BCBlockEntities.LIBRARY.get(), pos, state);
    }

    public boolean hasStored() {
        return storedBlueprint != null;
    }

    @Nullable
    public CompoundTag getStored() {
        return storedBlueprint == null ? null : storedBlueprint.copy();
    }

    public void setStored(@Nullable CompoundTag blueprint) {
        this.storedBlueprint = blueprint == null ? null : blueprint.copy();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (storedBlueprint != null) {
            tag.put("stored", storedBlueprint);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedBlueprint = tag.contains("stored") ? tag.getCompound("stored") : null;
    }
}
