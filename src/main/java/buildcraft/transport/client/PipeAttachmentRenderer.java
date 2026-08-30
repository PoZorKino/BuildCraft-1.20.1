/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

import buildcraft.transport.pipe.IPipeHolder;
import buildcraft.transport.pipe.PipeAttachment;

/** Shared coloured-box drawing for plugs, wires, and facades on any pipe holder. */
public final class PipeAttachmentRenderer {

    private PipeAttachmentRenderer() {}

    public static void render(IPipeHolder holder, PoseStack pose, MultiBufferSource buffers) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.lightning());
        Matrix4f matrix = pose.last().pose();
        for (Direction side : Direction.values()) {
            PipeAttachment attachment = holder.getAttachment(side);
            if (attachment == null) {
                continue;
            }
            renderAttachment(buffer, matrix, side, attachment, holder.isWirePowered());
        }
    }

    private static void renderAttachment(VertexConsumer buffer, Matrix4f matrix, Direction side,
            PipeAttachment attachment, boolean wirePowered) {
        float[] box = faceBox(side, attachment.kind);
        int color = switch (attachment.kind) {
            case PLUG -> 0xFF2A2A2A;
            case WIRE -> {
                DyeColor dye = attachment.color == null ? DyeColor.WHITE : attachment.color;
                int rgb = dye.getTextColor();
                if (wirePowered) {
                    rgb = brighten(rgb);
                }
                yield 0xFF000000 | rgb;
            }
            case FACADE -> 0xFF000000 | attachment.facadeAsBlock().defaultMapColor().col;
        };
        fillBox(buffer, matrix, box[0], box[1], box[2], box[3], box[4], box[5], color);
    }

    private static int brighten(int rgb) {
        int r = Math.min(255, ((rgb >> 16) & 0xFF) + 60);
        int g = Math.min(255, ((rgb >> 8) & 0xFF) + 60);
        int b = Math.min(255, (rgb & 0xFF) + 60);
        return (r << 16) | (g << 8) | b;
    }

    /** minX, minY, minZ, maxX, maxY, maxZ in 0..1 block space. */
    private static float[] faceBox(Direction side, PipeAttachment.Kind kind) {
        float depth = kind == PipeAttachment.Kind.FACADE ? 2 / 16f : kind == PipeAttachment.Kind.WIRE ? 1 / 16f : 2 / 16f;
        float inset = kind == PipeAttachment.Kind.FACADE ? 0f : kind == PipeAttachment.Kind.WIRE ? 6 / 16f : 4 / 16f;
        float min = inset;
        float max = 1f - inset;
        return switch (side) {
            case DOWN -> new float[] { min, 0f, min, max, depth, max };
            case UP -> new float[] { min, 1f - depth, min, max, 1f, max };
            case NORTH -> new float[] { min, min, 0f, max, max, depth };
            case SOUTH -> new float[] { min, min, 1f - depth, max, max, 1f };
            case WEST -> new float[] { 0f, min, min, depth, max, max };
            case EAST -> new float[] { 1f - depth, min, min, 1f, max, max };
        };
    }

    private static void fillBox(VertexConsumer buffer, Matrix4f matrix,
            float x0, float y0, float z0, float x1, float y1, float z1, int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
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
