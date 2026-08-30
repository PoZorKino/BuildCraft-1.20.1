/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import buildcraft.energy.client.EngineScreen;
import buildcraft.energy.tile.TileEngineBase;
import buildcraft.registry.BCBlocks;
import buildcraft.registry.BCMenuTypes;

/** Client-only bootstrap: screens and colour handlers. No-op on dedicated servers. */
public final class BuildCraftClient {

    private static final int[] STAGE_COLORS = {
            0xFF4060FF, 0xFF30C030, 0xFFFFD030, 0xFFFF4020, 0xFFFF10E0
    };

    public static void init(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(BuildCraftClient::onClientSetup);
            modBus.addListener(BuildCraftClient::registerBlockColors);
            modBus.addListener(BuildCraftClient::registerItemColors);
        }
    }

    private static Block[] engines() {
        return new Block[] {
                BCBlocks.ENGINE_WOOD.get(),
                BCBlocks.ENGINE_STONE.get(),
                BCBlocks.ENGINE_IRON.get(),
                BCBlocks.ENGINE_CREATIVE.get()
        };
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(BCMenuTypes.ENGINE.get(), EngineScreen::new));
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 1) {
                return -1;
            }
            if (level != null && pos != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TileEngineBase engine) {
                    return STAGE_COLORS[engine.getPowerStage()];
                }
            }
            return STAGE_COLORS[0];
        }, engines());
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 1 ? STAGE_COLORS[0] : -1, engines());
    }

    private BuildCraftClient() {}
}
