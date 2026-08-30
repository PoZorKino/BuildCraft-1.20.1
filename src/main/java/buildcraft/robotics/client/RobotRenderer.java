/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import buildcraft.BuildCraft;
import buildcraft.registry.BCItems;
import buildcraft.robotics.entity.RobotEntity;

/** Renders the robot as its floating, gently-bobbing item icon. */
public class RobotRenderer extends EntityRenderer<RobotEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(BuildCraft.MOD_ID, "textures/item/robot.png");
    private final ItemStack icon;

    public RobotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.icon = new ItemStack(BCItems.ROBOT.get());
    }

    @Override
    public void render(RobotEntity entity, float yaw, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        float bob = (float) Math.sin((entity.tickCount + partialTick) * 0.15F) * 0.08F;
        pose.translate(0.0, 0.4 + bob, 0.0);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 4.0F));
        pose.scale(0.75F, 0.75F, 0.75F);
        Minecraft.getInstance().getItemRenderer().renderStatic(icon, ItemDisplayContext.FIXED,
                packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                pose, buffers, entity.level(), 0);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RobotEntity entity) {
        return TEXTURE;
    }
}
