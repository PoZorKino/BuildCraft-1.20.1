/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.data;

import java.util.Set;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.registry.BCBlocks;

public class BCBlockLoot extends BlockLootSubProvider {

    public BCBlockLoot() {
        super(Set.of(), FeatureFlags.VANILLA_SET);
    }

    @Override
    protected void generate() {
        for (RegistryObject<Block> holder : BCBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (block instanceof LiquidBlock) {
                add(block, noDrop());
            } else {
                dropSelf(block);
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BCBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).toList();
    }
}
