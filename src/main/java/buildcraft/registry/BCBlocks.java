/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.energy.block.BlockEngineStone;

/** All blocks (and their block items) ported from BuildCraft. */
public final class BCBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BuildCraft.MOD_ID);

    public static final RegistryObject<Block> ENGINE_STONE = BLOCKS.register("engine_stone",
            () -> new BlockEngineStone(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)));

    static {
        // Register a matching BlockItem for every block.
        BCItems.ITEMS.register("engine_stone",
                () -> new BlockItem(ENGINE_STONE.get(), new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private BCBlocks() {}
}
