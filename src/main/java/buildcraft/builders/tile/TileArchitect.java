/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import buildcraft.builders.BlueprintData;
import buildcraft.builders.block.BlockBuilderMachine;
import buildcraft.factory.tile.ITickingMachine;
import buildcraft.factory.util.MachineEnergyStorage;
import buildcraft.registry.BCBlockEntities;

/** Architect: scans the cuboid in front of it into a blank Template once enough energy is stored. */
public class TileArchitect extends BlockEntity implements ITickingMachine, TemplateHolder {

    public static final int CAPACITY = 20_000;
    public static final int MAX_RECEIVE = 1_000;
    public static final int SCAN_COST = 5_000;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private ItemStack template = ItemStack.EMPTY;

    public TileArchitect(BlockPos pos, BlockState state) {
        super(BCBlockEntities.ARCHITECT.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (template.isEmpty() || BlueprintData.hasData(template)) {
            return;
        }
        if (energy.getEnergyStored() < SCAN_COST) {
            return;
        }
        Direction facing = state.getValue(BlockBuilderMachine.FACING);
        BlockPos origin = BlueprintData.regionOrigin(pos, facing);
        BlueprintData.scan(level, origin, template);
        energy.spend(SCAN_COST);
        setChanged();
    }

    @Override
    public ItemStack getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(ItemStack stack) {
        this.template = stack;
        setChanged();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energy.getEnergyStored());
        tag.put("template", template.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        template = ItemStack.of(tag.getCompound("template"));
    }
}
