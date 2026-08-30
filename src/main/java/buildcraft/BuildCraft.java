/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import buildcraft.client.BuildCraftClient;
import buildcraft.energy.fluid.BCFluids;
import buildcraft.registry.BCBlockEntities;
import buildcraft.registry.BCBlocks;
import buildcraft.registry.BCCreativeTabs;
import buildcraft.registry.BCEntities;
import buildcraft.registry.BCFeatures;
import buildcraft.registry.BCItems;
import buildcraft.registry.BCMenuTypes;

/**
 * Main entry point of BuildCraft ported to Minecraft 1.20.1 / Forge.
 *
 * <p>The legacy 1.12.2 code base lives under {@code common/} and is intentionally excluded from
 * compilation. This class wires up the modern deferred registries that back the ported content.
 */
@Mod(BuildCraft.MOD_ID)
public class BuildCraft {
    public static final String MOD_ID = "buildcraft";

    public BuildCraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BCItems.register(modBus);
        BCBlocks.register(modBus);
        BCBlockEntities.register(modBus);
        BCMenuTypes.register(modBus);
        BCFluids.register(modBus);
        BCEntities.register(modBus);
        BCFeatures.register(modBus);
        BCCreativeTabs.register(modBus);

        BuildCraftClient.init(modBus);

        BCLog.LOGGER.info("BuildCraft (1.20.1 port) constructed.");
    }
}
