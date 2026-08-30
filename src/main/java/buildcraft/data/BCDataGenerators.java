/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import buildcraft.BuildCraft;

/** Registers Forge data generators for recipes, loot tables, and language. Run with {@code ./gradlew runData}. */
@Mod.EventBusSubscriber(modid = BuildCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BCDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new BCRecipeProvider(output));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BCBlockLoot::new, LootContextParamSets.BLOCK))));
        generator.addProvider(event.includeClient(), new BCLangProvider(output));
        if (files != null) {
            generator.addProvider(event.includeClient(), new BCBlockStateProvider(output, files));
        }
    }

    private BCDataGenerators() {}
}
