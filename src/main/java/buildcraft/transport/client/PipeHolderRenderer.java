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
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.transport.pipe.IPipeHolder;

/** BER for fluid and power pipes: draws plugs, wires, and facades. */
public class PipeHolderRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    public PipeHolderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T be, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        if (be instanceof IPipeHolder holder) {
            PipeAttachmentRenderer.render(holder, pose, buffers);
        }
    }
}
