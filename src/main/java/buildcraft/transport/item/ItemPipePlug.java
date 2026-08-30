/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.item;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.transport.pipe.IPipeHolder;
import buildcraft.transport.pipe.PipeAttachment;

/** A blocking plug that covers one face of a pipe and stops connections through that side. */
public class ItemPipePlug extends Item {

    public ItemPipePlug(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (be instanceof IPipeHolder holder) {
            Direction side = context.getClickedFace();
            if (holder.attach(side, PipeAttachment.plug())) {
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
