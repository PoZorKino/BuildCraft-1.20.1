/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.recipe;

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

import buildcraft.registry.BCItems;
import buildcraft.registry.BCRecipeSerializers;
import buildcraft.transport.item.ItemPipeWire;

/** Shapeless: pipe wire + any dye → coloured wire. */
public class DyeWireRecipe extends CustomRecipe {

    public DyeWireRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return extract(container) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        DyeColor color = extract(container);
        return color == null ? ItemStack.EMPTY : ItemPipeWire.withColor(BCItems.PIPE_WIRE.get(), color);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BCRecipeSerializers.DYE_WIRE.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return new ItemStack(BCItems.PIPE_WIRE.get());
    }

    @javax.annotation.Nullable
    private static DyeColor extract(CraftingContainer container) {
        ItemStack wire = ItemStack.EMPTY;
        DyeColor color = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(BCItems.PIPE_WIRE.get())) {
                if (!wire.isEmpty()) {
                    return null;
                }
                wire = stack;
            } else if (stack.getItem() instanceof DyeItem dye) {
                if (color != null) {
                    return null;
                }
                color = dye.getDyeColor();
            } else {
                return null;
            }
        }
        return !wire.isEmpty() && color != null ? color : null;
    }
}
