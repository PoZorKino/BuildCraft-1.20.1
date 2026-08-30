/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.energy.worldgen.OilFeature;

public final class BCFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, BuildCraft.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OIL =
            FEATURES.register("oil", () -> new OilFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private BCFeatures() {}
}
