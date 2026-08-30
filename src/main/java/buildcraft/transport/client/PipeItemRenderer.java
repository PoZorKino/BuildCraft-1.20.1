/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

import buildcraft.transport.tile.TilePipe;

/** Renders travelling items and any plugs / wires / facades attached to a pipe. */
public class PipeItemRenderer implements BlockEntityRenderer<TilePipe> {

    private final ItemRenderer itemRenderer;

    public PipeItemRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TilePipe pipe, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        if (pipe.getLevel() == null) {
            return;
        }
        int transit = Math.max(1, pipe.getTransitTicks());
        for (TilePipe.TravelingItem item : pipe.getTravelingItems()) {
            if (item.stack.isEmpty()) {
                continue;
            }
            float progress = Math.min(1.0F, (item.age + partialTick) / transit);
            Direction from = item.from;
            float offset = 0.5F - progress;
            double x = 0.5 + from.getStepX() * offset;
            double y = 0.5 + from.getStepY() * offset;
            double z = 0.5 + from.getStepZ() * offset;

            pose.pushPose();
            pose.translate(x, y, z);
            pose.scale(0.4F, 0.4F, 0.4F);
            itemRenderer.renderStatic(item.stack, ItemDisplayContext.GROUND, packedLight, packedOverlay,
                    pose, buffers, pipe.getLevel(), 0);
            pose.popPose();
        }

        PipeAttachmentRenderer.render(pipe, pose, buffers);
    }
}
