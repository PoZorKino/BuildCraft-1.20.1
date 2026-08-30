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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.transport.pipe.IPipeHolder;
import buildcraft.transport.pipe.PipeAttachment;

/** Coloured pipe wire: attaches to a pipe face and conducts redstone along matching wires. */
public class ItemPipeWire extends Item {

    public ItemPipeWire(Properties props) {
        super(props);
    }

    public static DyeColor getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("color")) {
            DyeColor color = DyeColor.byName(tag.getString("color"), null);
            if (color != null) {
                return color;
            }
        }
        return DyeColor.WHITE;
    }

    public static ItemStack withColor(Item item, DyeColor color) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString("color", color.getName());
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof IPipeHolder holder) {
            Direction side = context.getClickedFace();
            if (holder.attach(side, PipeAttachment.wire(getColor(context.getItemInHand())))) {
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.buildcraft.pipe_wire.color",
                Component.translatable("color.minecraft." + getColor(stack).getName())));
    }
}
