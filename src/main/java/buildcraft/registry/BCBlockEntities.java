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
import buildcraft.energy.tile.StirlingEngineBlockEntity;

public final class BCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildCraft.MOD_ID);

    public static final RegistryObject<BlockEntityType<StirlingEngineBlockEntity>> ENGINE_STONE =
            BLOCK_ENTITIES.register("engine_stone", () -> BlockEntityType.Builder
                    .of(StirlingEngineBlockEntity::new, BCBlocks.ENGINE_STONE.get())
                    .build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private BCBlockEntities() {}
}
