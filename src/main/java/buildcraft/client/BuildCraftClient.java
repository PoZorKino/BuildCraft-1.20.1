/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import buildcraft.energy.client.EngineScreen;
import buildcraft.energy.tile.TileEngineBase;
import buildcraft.registry.BCBlocks;
import buildcraft.registry.BCItems;
import buildcraft.registry.BCMenuTypes;

/** Client-only bootstrap: screens and colour handlers. No-op on dedicated servers. */
public final class BuildCraftClient {

    private static final int[] STAGE_COLORS = {
            0xFF4060FF, 0xFF30C030, 0xFFFFD030, 0xFFFF4020, 0xFFFF10E0
    };

    public static void init(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(BuildCraftClient::onClientSetup);
            modBus.addListener(BuildCraftClient::registerBlockColors);
            modBus.addListener(BuildCraftClient::registerItemColors);
            modBus.addListener(BuildCraftClient::registerRenderers);
        }
    }

    private static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_WOOD.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_COBBLESTONE.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_STONE.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_GOLD.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_IRON.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_DIAMOND.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_OBSIDIAN.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_VOID.get(),
                buildcraft.transport.client.PipeItemRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_FLUID_WOOD.get(),
                buildcraft.transport.client.PipeHolderRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_FLUID_COBBLESTONE.get(),
                buildcraft.transport.client.PipeHolderRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_POWER_WOOD.get(),
                buildcraft.transport.client.PipeHolderRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.PIPE_POWER_COBBLESTONE.get(),
                buildcraft.transport.client.PipeHolderRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.LASER.get(),
                buildcraft.silicon.client.LaserRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.ENGINE_WOOD.get(),
                buildcraft.energy.client.EngineRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.ENGINE_STONE.get(),
                buildcraft.energy.client.EngineRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.ENGINE_IRON.get(),
                buildcraft.energy.client.EngineRenderer::new);
        event.registerBlockEntityRenderer(buildcraft.registry.BCBlockEntities.ENGINE_CREATIVE.get(),
                buildcraft.energy.client.EngineRenderer::new);
        event.registerEntityRenderer(buildcraft.registry.BCEntities.ROBOT.get(),
                buildcraft.robotics.client.RobotRenderer::new);
    }

    private static Block[] engines() {
        return new Block[] {
                BCBlocks.ENGINE_WOOD.get(),
                BCBlocks.ENGINE_STONE.get(),
                BCBlocks.ENGINE_IRON.get(),
                BCBlocks.ENGINE_CREATIVE.get()
        };
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(BCMenuTypes.ENGINE.get(), EngineScreen::new);
            MenuScreens.register(BCMenuTypes.ASSEMBLY_TABLE.get(),
                    buildcraft.silicon.client.AssemblyScreen::new);
            MenuScreens.register(BCMenuTypes.AUTO_WORKBENCH.get(),
                    buildcraft.factory.client.AutoWorkbenchScreen::new);
            MenuScreens.register(BCMenuTypes.INTEGRATION_TABLE.get(),
                    buildcraft.silicon.client.SiliconTableScreen::new);
            MenuScreens.register(BCMenuTypes.PROGRAMMING_TABLE.get(),
                    buildcraft.silicon.client.SiliconTableScreen::new);
            MenuScreens.register(BCMenuTypes.PIPE_DIAMOND.get(),
                    buildcraft.transport.client.DiamondPipeScreen::new);
            net.minecraft.client.renderer.item.ItemProperties.register(
                    buildcraft.registry.BCItems.TEMPLATE.get(),
                    new net.minecraft.resources.ResourceLocation(buildcraft.BuildCraft.MOD_ID, "used"),
                    (stack, lvl, entity, seed) -> buildcraft.builders.BlueprintData.hasData(stack) ? 1.0F : 0.0F);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    BCBlocks.TANK.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    buildcraft.energy.fluid.BCFluids.OIL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    buildcraft.energy.fluid.BCFluids.OIL_FLOWING.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    buildcraft.energy.fluid.BCFluids.FUEL.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    buildcraft.energy.fluid.BCFluids.FUEL_FLOWING.get(), net.minecraft.client.renderer.RenderType.translucent());
        });
    }

    private static Block[] pipes() {
        return new Block[] {
                BCBlocks.PIPE_WOOD.get(),
                BCBlocks.PIPE_COBBLESTONE.get(),
                BCBlocks.PIPE_STONE.get(),
                BCBlocks.PIPE_GOLD.get(),
                BCBlocks.PIPE_IRON.get(),
                BCBlocks.PIPE_DIAMOND.get(),
                BCBlocks.PIPE_OBSIDIAN.get(),
                BCBlocks.PIPE_VOID.get(),
                BCBlocks.PIPE_FLUID_WOOD.get(),
                BCBlocks.PIPE_FLUID_COBBLESTONE.get(),
                BCBlocks.PIPE_POWER_WOOD.get(),
                BCBlocks.PIPE_POWER_COBBLESTONE.get()
        };
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 1) {
                return -1;
            }
            if (level != null && pos != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TileEngineBase engine) {
                    return STAGE_COLORS[engine.getPowerStage()];
                }
            }
            return STAGE_COLORS[0];
        }, engines());
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            if (level != null && pos != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof buildcraft.transport.pipe.IPipeHolder holder && holder.getColor() != null) {
                    return 0xFF000000 | holder.getColor().getTextColor();
                }
            }
            return -1;
        }, pipes());
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 1 ? STAGE_COLORS[0] : -1, engines());
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            return 0xFF000000 | buildcraft.transport.item.ItemPipeWire.getColor(stack).getTextColor();
        }, BCItems.PIPE_WIRE.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            var color = buildcraft.core.item.ItemPaintbrush.getColor(stack);
            return color == null ? -1 : 0xFF000000 | color.getTextColor();
        }, BCItems.PAINTBRUSH.get());
    }

    private BuildCraftClient() {}
}
