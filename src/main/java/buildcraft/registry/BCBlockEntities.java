/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.energy.tile.TileEngineCreative;
import buildcraft.energy.tile.TileEngineIron;
import buildcraft.energy.tile.TileEngineStone;
import buildcraft.energy.tile.TileEngineWood;

public final class BCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildCraft.MOD_ID);

    public static final RegistryObject<BlockEntityType<TileEngineWood>> ENGINE_WOOD =
            BLOCK_ENTITIES.register("engine_wood", () -> BlockEntityType.Builder
                    .of(TileEngineWood::new, BCBlocks.ENGINE_WOOD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEngineStone>> ENGINE_STONE =
            BLOCK_ENTITIES.register("engine_stone", () -> BlockEntityType.Builder
                    .of(TileEngineStone::new, BCBlocks.ENGINE_STONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEngineIron>> ENGINE_IRON =
            BLOCK_ENTITIES.register("engine_iron", () -> BlockEntityType.Builder
                    .of(TileEngineIron::new, BCBlocks.ENGINE_IRON.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEngineCreative>> ENGINE_CREATIVE =
            BLOCK_ENTITIES.register("engine_creative", () -> BlockEntityType.Builder
                    .of(TileEngineCreative::new, BCBlocks.ENGINE_CREATIVE.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private BCBlockEntities() {}
}
