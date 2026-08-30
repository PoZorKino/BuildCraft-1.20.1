/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import buildcraft.core.item.ItemPaintbrush;
import buildcraft.registry.BCItems;
import buildcraft.registry.BCRecipeSerializers;

/** Shapeless: paintbrush + dye → loaded paintbrush. */
public class DyePaintbrushRecipe extends CustomRecipe {

    public DyePaintbrushRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return extract(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        DyeColor color = extract(container);
        return color == null ? ItemStack.EMPTY : ItemPaintbrush.withColor(BCItems.PAINTBRUSH.get(), color);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BCRecipeSerializers.DYE_PAINTBRUSH.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return new ItemStack(BCItems.PAINTBRUSH.get());
    }

    @javax.annotation.Nullable
    private static DyeColor extract(CraftingContainer container) {
        boolean brush = false;
        DyeColor color = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(BCItems.PAINTBRUSH.get())) {
                if (brush) {
                    return null;
                }
                brush = true;
            } else if (stack.getItem() instanceof DyeItem dye) {
                if (color != null) {
                    return null;
                }
                color = dye.getDyeColor();
            } else {
                return null;
            }
        }
        return brush && color != null ? color : null;
    }
}
