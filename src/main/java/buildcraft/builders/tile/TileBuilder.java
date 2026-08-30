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

/** Builder: rebuilds the structure stored in its Template, one block at a time, using energy. */
public class TileBuilder extends BlockEntity implements ITickingMachine, TemplateHolder {

    public static final int CAPACITY = 50_000;
    public static final int MAX_RECEIVE = 2_000;
    public static final int BUILD_COST = 200;
    public static final int BUILD_INTERVAL = 3;

    private final MachineEnergyStorage energy = new MachineEnergyStorage(CAPACITY, MAX_RECEIVE);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energy);
    private ItemStack template = ItemStack.EMPTY;
    private int buildIndex;
    private int cooldown;

    public TileBuilder(BlockPos pos, BlockState state) {
        super(BCBlockEntities.BUILDER.get(), pos, state);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (template.isEmpty() || !BlueprintData.hasData(template)) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (energy.getEnergyStored() < BUILD_COST) {
            return;
        }
        BlueprintData data = BlueprintData.read(template);
        if (buildIndex >= data.volume()) {
            return; // Finished.
        }
        Direction facing = state.getValue(BlockBuilderMachine.FACING);
        BlockPos origin = BlueprintData.regionOrigin(pos, facing);

        while (buildIndex < data.volume()) {
            BlockState toPlace = data.stateAt(buildIndex);
            BlockPos target = origin.offset(data.offsetFor(buildIndex));
            if (toPlace == null || target.equals(getBlockPos()) || !level.getBlockState(target).canBeReplaced()) {
                buildIndex++;
                continue;
            }
            if (!consumeMaterial(level, pos, toPlace)) {
                return;
            }
            level.setBlock(target, toPlace, 3);
            buildIndex++;
            energy.spend(BUILD_COST);
            cooldown = BUILD_INTERVAL;
            setChanged();
            return;
        }
        setChanged();
    }

    /** Pull one matching block item from a neighbouring inventory. */
    private boolean consumeMaterial(Level level, BlockPos pos, BlockState toPlace) {
        net.minecraft.world.item.Item wanted = toPlace.getBlock().asItem();
        if (wanted == net.minecraft.world.item.Items.AIR) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) {
                continue;
            }
            net.minecraftforge.items.IItemHandler handler =
                    neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            if (handler == null) {
                continue;
            }
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (handler.getStackInSlot(slot).is(wanted) && !handler.extractItem(slot, 1, false).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(ItemStack stack) {
        this.template = stack;
        this.buildIndex = 0;
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
        tag.putInt("buildIndex", buildIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergyStored(tag.getInt("energy"));
        template = ItemStack.of(tag.getCompound("template"));
        buildIndex = tag.getInt("buildIndex");
    }
}
