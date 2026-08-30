/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import buildcraft.transport.block.BlockPipe;
import buildcraft.transport.pipe.PipeAttachment;
import buildcraft.transport.tile.TilePipe;

/**
 * Facade: sneak-right-click a solid block to copy it, then right-click a pipe face to cover that
 * side. Facades block pipe connections through the covered face.
 */
public class ItemFacade extends Item {

    public ItemFacade(Properties props) {
        super(props);
    }

    @Nullable
    public static String getCopiedBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("block")) {
            return tag.getString("block");
        }
        return null;
    }

    public static ItemStack withBlock(Item item, Block block) {
        ItemStack stack = new ItemStack(item);
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        if (id != null) {
            stack.getOrCreateTag().putString("block", id.toString());
        }
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof TilePipe pipe) {
            String copied = getCopiedBlock(context.getItemInHand());
            if (copied == null) {
                return InteractionResult.FAIL;
            }
            Direction side = context.getClickedFace();
            if (pipe.attach(side, PipeAttachment.facade(copied))) {
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            return InteractionResult.FAIL;
        }
        if (!(state.getBlock() instanceof BlockPipe) && context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            if (!state.isAir() && state.isCollisionShapeFullBlock(context.getLevel(), context.getClickedPos())) {
                if (!context.getLevel().isClientSide) {
                    ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    if (id != null) {
                        context.getItemInHand().getOrCreateTag().putString("block", id.toString());
                    }
                }
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String copied = getCopiedBlock(stack);
        if (copied == null) {
            tooltip.add(Component.translatable("item.buildcraft.pipe_facade.empty"));
        } else {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(copied));
            Component name = block == null ? Component.literal(copied) : block.getName();
            tooltip.add(Component.translatable("item.buildcraft.pipe_facade.copied", name));
        }
    }
}
