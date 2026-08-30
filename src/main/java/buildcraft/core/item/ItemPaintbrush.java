/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.transport.pipe.IPipeHolder;

/**
 * Paintbrush: craft with a dye to load a colour, then right-click pipes (or wool/concrete/glass)
 * to paint them. An unloaded brush wipes colour off a pipe.
 */
public class ItemPaintbrush extends Item {

    public static final int MAX_USES = 64;

    public ItemPaintbrush(Properties props) {
        super(props);
    }

    @Nullable
    public static DyeColor getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("color")) {
            return DyeColor.byName(tag.getString("color"), null);
        }
        return null;
    }

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("uses")) {
            return getColor(stack) == null ? 0 : MAX_USES;
        }
        return tag.getInt("uses");
    }

    public static ItemStack withColor(Item item, @Nullable DyeColor color) {
        ItemStack stack = new ItemStack(item);
        if (color != null) {
            stack.getOrCreateTag().putString("color", color.getName());
            stack.getOrCreateTag().putInt("uses", MAX_USES);
        }
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        DyeColor color = getColor(stack);
        int uses = getUses(stack);

        if (be instanceof IPipeHolder holder) {
            if (color != null && uses <= 0) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                if (holder.setColor(color)) {
                    consumeUse(context, stack, color);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (color != null && uses > 0) {
            BlockState state = level.getBlockState(context.getClickedPos());
            Block painted = recolor(state.getBlock(), color);
            if (painted != null && painted != state.getBlock()) {
                if (!level.isClientSide) {
                    level.setBlock(context.getClickedPos(), painted.withPropertiesOf(state), 3);
                    consumeUse(context, stack, color);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    private static void consumeUse(UseOnContext context, ItemStack stack, @Nullable DyeColor color) {
        if (color == null || context.getPlayer() != null && context.getPlayer().getAbilities().instabuild) {
            return;
        }
        int uses = getUses(stack) - 1;
        if (uses <= 0) {
            stack.removeTagKey("color");
            stack.removeTagKey("uses");
        } else {
            stack.getOrCreateTag().putInt("uses", uses);
        }
    }

    @Nullable
    private static Block recolor(Block block, DyeColor color) {
        var id = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        String path = id == null ? "" : id.getPath();
        String[] suffixes = { "_wool", "_carpet", "_terracotta", "_concrete", "_concrete_powder",
                "_stained_glass", "_stained_glass_pane" };
        for (String suffix : suffixes) {
            if (path.endsWith(suffix)) {
                return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                        new net.minecraft.resources.ResourceLocation("minecraft", color.getName() + suffix));
            }
        }
        if (block == Blocks.GLASS) {
            return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new net.minecraft.resources.ResourceLocation("minecraft", color.getName() + "_stained_glass"));
        }
        if (block == Blocks.GLASS_PANE) {
            return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new net.minecraft.resources.ResourceLocation("minecraft", color.getName() + "_stained_glass_pane"));
        }
        if (block == Blocks.TERRACOTTA) {
            return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new net.minecraft.resources.ResourceLocation("minecraft", color.getName() + "_terracotta"));
        }
        return null;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getColor(stack) != null && getUses(stack) < MAX_USES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getUses(stack) / (float) MAX_USES);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        DyeColor color = getColor(stack);
        if (color == null) {
            tooltip.add(Component.translatable("item.buildcraft.paintbrush.clean"));
        } else {
            tooltip.add(Component.translatable("item.buildcraft.paintbrush.color",
                    Component.translatable("color.minecraft." + color.getName())));
            tooltip.add(Component.translatable("item.buildcraft.paintbrush.uses", getUses(stack), MAX_USES));
        }
    }
}
