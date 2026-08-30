/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.tile.TileLaser;

/** Renders the Laser's red energy beam towards the block it is powering. */
public class LaserRenderer implements BlockEntityRenderer<TileLaser> {

    private static final float HALF = 0.08F;

    public LaserRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileLaser laser, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        if (laser.getEnergyStored() <= 0 || laser.getLevel() == null) {
            return;
        }
        Direction facing = laser.getBlockState().getValue(BlockLaser.FACING);

        Vector3f axis = new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vector3f u;
        Vector3f v;
        if (facing.getAxis() == Direction.Axis.Y) {
            u = new Vector3f(1, 0, 0);
            v = new Vector3f(0, 0, 1);
        } else if (facing.getAxis() == Direction.Axis.Z) {
            u = new Vector3f(1, 0, 0);
            v = new Vector3f(0, 1, 0);
        } else {
            u = new Vector3f(0, 1, 0);
            v = new Vector3f(0, 0, 1);
        }
        u.mul(HALF);
        v.mul(HALF);

        Vector3f start = new Vector3f(0.5F, 0.5F, 0.5F).add(new Vector3f(axis).mul(0.25F));
        Vector3f end = new Vector3f(0.5F, 0.5F, 0.5F).add(new Vector3f(axis).mul(1.2F));

        float time = (laser.getLevel().getGameTime() + partialTick);
        int alpha = (int) (170 + 60 * Math.sin(time * 0.4));
        alpha = Math.max(120, Math.min(255, alpha));

        VertexConsumer buffer = buffers.getBuffer(RenderType.lightning());
        Matrix4f matrix = pose.last().pose();
        // Four faces of a rectangular beam tube.
        beamFace(buffer, matrix, start, end, u, v, alpha);
        beamFace(buffer, matrix, start, end, negate(u), v, alpha);
        beamFace(buffer, matrix, start, end, v, u, alpha);
        beamFace(buffer, matrix, start, end, negate(v), u, alpha);
    }

    private static Vector3f negate(Vector3f in) {
        return new Vector3f(-in.x, -in.y, -in.z);
    }

    private void beamFace(VertexConsumer buffer, Matrix4f matrix, Vector3f start, Vector3f end,
            Vector3f offset, Vector3f width, int alpha) {
        Vector3f a = add(start, offset, width, -1);
        Vector3f b = add(start, offset, width, 1);
        Vector3f c = add(end, offset, width, 1);
        Vector3f d = add(end, offset, width, -1);
        vertex(buffer, matrix, a, alpha);
        vertex(buffer, matrix, b, alpha);
        vertex(buffer, matrix, c, alpha);
        vertex(buffer, matrix, d, alpha);
    }

    private static Vector3f add(Vector3f base, Vector3f offset, Vector3f width, float sign) {
        return new Vector3f(base.x + offset.x + width.x * 0.5F * sign,
                base.y + offset.y + width.y * 0.5F * sign,
                base.z + offset.z + width.z * 0.5F * sign);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Vector3f p, int alpha) {
        buffer.vertex(matrix, p.x, p.y, p.z).color(255, 40, 20, alpha).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(TileLaser be) {
        return true;
    }
}
