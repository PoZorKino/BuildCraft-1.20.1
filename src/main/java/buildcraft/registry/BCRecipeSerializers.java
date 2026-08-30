/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.core.recipe.DyePaintbrushRecipe;
import buildcraft.transport.recipe.DyeWireRecipe;

public final class BCRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, BuildCraft.MOD_ID);

    public static final RegistryObject<RecipeSerializer<DyeWireRecipe>> DYE_WIRE =
            SERIALIZERS.register("dye_wire", () -> new SimpleCraftingRecipeSerializer<>(DyeWireRecipe::new));

    public static final RegistryObject<RecipeSerializer<DyePaintbrushRecipe>> DYE_PAINTBRUSH =
            SERIALIZERS.register("dye_paintbrush", () -> new SimpleCraftingRecipeSerializer<>(DyePaintbrushRecipe::new));

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    private BCRecipeSerializers() {}
}
