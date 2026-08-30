/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import buildcraft.energy.block.BlockEngine;
import buildcraft.energy.tile.TileEngineBase;

/**
 * Draws an extending trunk along the engine's facing, oscillating while the engine is pumping.
 * The static block model still shows the resting trunk; this overlay makes the motion visible.
 */
public class EngineRenderer implements BlockEntityRenderer<TileEngineBase> {

    private static final int[] STAGE_COLORS = {
            0xFF4060FF, 0xFF30C030, 0xFFFFD030, 0xFFFF4020, 0xFFFF10E0
    };

    public EngineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEngineBase engine, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        if (engine.getLevel() == null) {
            return;
        }
        Direction facing = engine.getBlockState().getValue(BlockEngine.FACING);
        float extension = engine.getPistonExtension(partialTick);
        if (extension <= 0.01F) {
            return;
        }

        int color = STAGE_COLORS[Math.min(STAGE_COLORS.length - 1, engine.getPowerStage())];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        float depth = 0.15F + extension * 0.35F;
        float min = 4 / 16f;
        float max = 12 / 16f;
        float[] box = switch (facing) {
            case UP -> new float[] { min, 10 / 16f, min, max, 10 / 16f + depth, max };
            case DOWN -> new float[] { min, 6 / 16f - depth, min, max, 6 / 16f, max };
            case NORTH -> new float[] { min, min, 6 / 16f - depth, max, max, 6 / 16f };
            case SOUTH -> new float[] { min, min, 10 / 16f, max, max, 10 / 16f + depth };
            case WEST -> new float[] { 6 / 16f - depth, min, min, 6 / 16f, max, max };
            case EAST -> new float[] { 10 / 16f, min, min, 10 / 16f + depth, max, max };
        };

        VertexConsumer buffer = buffers.getBuffer(RenderType.lightning());
        fillBox(buffer, pose.last().pose(), box[0], box[1], box[2], box[3], box[4], box[5], r, g, b, 220);
    }

    private static void fillBox(VertexConsumer buffer, Matrix4f matrix,
            float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int a) {
        quad(buffer, matrix, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a);
        quad(buffer, matrix, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, r, g, b, a);
        quad(buffer, matrix, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
        quad(buffer, matrix, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a);
        quad(buffer, matrix, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
        quad(buffer, matrix, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a);
    }

    private static void quad(VertexConsumer buffer, Matrix4f matrix,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float x2, float y2, float z2, float x3, float y3, float z3,
            int r, int g, int b, int a) {
        buffer.vertex(matrix, x0, y0, z0).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a).endVertex();
    }
}
