/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.data;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import buildcraft.registry.BCBlocks;
import buildcraft.registry.BCItems;
import buildcraft.registry.BCRecipeSerializers;

public class BCRecipeProvider extends RecipeProvider {

    public BCRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBlocks.PIPE_IRON.get(), 8)
                .pattern("igi")
                .define('i', Items.IRON_INGOT)
                .define('g', Blocks.GLASS)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBlocks.PIPE_DIAMOND.get(), 8)
                .pattern("dgd")
                .define('d', Items.DIAMOND)
                .define('g', Blocks.GLASS)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCItems.PIPE_PLUG.get(), 4)
                .pattern(" c ")
                .pattern("csc")
                .pattern(" c ")
                .define('c', Blocks.COBBLESTONE)
                .define('s', Items.STICK)
                .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCItems.PIPE_WIRE.get(), 8)
                .requires(Items.REDSTONE)
                .requires(Items.STRING)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCItems.PIPE_FACADE.get(), 6)
                .pattern(" p ")
                .pattern("p p")
                .pattern(" p ")
                .define('p', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCItems.PAINTBRUSH.get())
                .pattern(" iw")
                .pattern(" gi")
                .pattern("s  ")
                .define('i', Items.STRING)
                .define('w', Blocks.WHITE_WOOL)
                .define('g', BCItems.GEAR_WOOD.get())
                .define('s', Items.STICK)
                .unlockedBy("has_gear", has(BCItems.GEAR_WOOD.get()))
                .save(writer);
        SpecialRecipeBuilder.special(BCRecipeSerializers.DYE_WIRE.get()).save(writer, "buildcraft:dye_wire");
        SpecialRecipeBuilder.special(BCRecipeSerializers.DYE_PAINTBRUSH.get()).save(writer, "buildcraft:dye_paintbrush");
    }
}
