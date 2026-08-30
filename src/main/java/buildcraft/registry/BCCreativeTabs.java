/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;

public final class BCCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BuildCraft.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.buildcraft"))
            .icon(() -> new ItemStack(BCItems.WRENCH.get()))
            .displayItems((params, output) -> {
                output.accept(BCItems.GEAR_WOOD.get());
                output.accept(BCItems.GEAR_STONE.get());
                output.accept(BCItems.GEAR_IRON.get());
                output.accept(BCItems.GEAR_GOLD.get());
                output.accept(BCItems.GEAR_DIAMOND.get());
                output.accept(BCItems.DIAMOND_SHARD.get());
                output.accept(BCItems.CHIPSET_REDSTONE.get());
                output.accept(BCItems.CHIPSET_IRON.get());
                output.accept(BCItems.CHIPSET_GOLD.get());
                output.accept(BCItems.CHIPSET_DIAMOND.get());
                output.accept(BCItems.CHIPSET_QUARTZ.get());
                output.accept(BCItems.TEMPLATE.get());
                output.accept(BCItems.ROBOT.get());
                output.accept(BCItems.BOARD_BLANK.get());
                output.accept(BCItems.BOARD_RED.get());
                output.accept(BCItems.BOARD_GREEN.get());
                output.accept(BCItems.BOARD_BLUE.get());
                output.accept(BCItems.WRENCH.get());
                output.accept(BCItems.PAINTBRUSH.get());
                for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values()) {
                    output.accept(buildcraft.core.item.ItemPaintbrush.withColor(BCItems.PAINTBRUSH.get(), color));
                }
                output.accept(BCItems.PIPE_PLUG.get());
                for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values()) {
                    output.accept(buildcraft.transport.item.ItemPipeWire.withColor(BCItems.PIPE_WIRE.get(), color));
                }
                output.accept(BCItems.PIPE_FACADE.get());
                output.accept(BCBlocks.ENGINE_WOOD.get());
                output.accept(BCBlocks.ENGINE_STONE.get());
                output.accept(BCBlocks.ENGINE_IRON.get());
                output.accept(BCBlocks.ENGINE_CREATIVE.get());
                output.accept(buildcraft.energy.fluid.BCFluids.OIL_BUCKET.get());
                output.accept(buildcraft.energy.fluid.BCFluids.FUEL_BUCKET.get());
                output.accept(BCBlocks.TANK.get());
                output.accept(BCBlocks.PUMP.get());
                output.accept(BCBlocks.MINING_WELL.get());
                output.accept(BCBlocks.REFINERY.get());
                output.accept(BCBlocks.DISTILLER.get());
                output.accept(BCBlocks.FLOODGATE.get());
                output.accept(BCBlocks.HEAT_EXCHANGER.get());
                output.accept(BCBlocks.CHUTE.get());
                output.accept(BCBlocks.AUTO_WORKBENCH.get());
                output.accept(BCBlocks.QUARRY.get());
                output.accept(BCBlocks.PIPE_WOOD.get());
                output.accept(BCBlocks.PIPE_COBBLESTONE.get());
                output.accept(BCBlocks.PIPE_STONE.get());
                output.accept(BCBlocks.PIPE_GOLD.get());
                output.accept(BCBlocks.PIPE_IRON.get());
                output.accept(BCBlocks.PIPE_DIAMOND.get());
                output.accept(BCBlocks.PIPE_OBSIDIAN.get());
                output.accept(BCBlocks.PIPE_VOID.get());
                output.accept(BCBlocks.PIPE_FLUID_WOOD.get());
                output.accept(BCBlocks.PIPE_FLUID_COBBLESTONE.get());
                output.accept(BCBlocks.PIPE_POWER_WOOD.get());
                output.accept(BCBlocks.PIPE_POWER_COBBLESTONE.get());
                output.accept(BCBlocks.GATE.get());
                output.accept(BCBlocks.ASSEMBLY_TABLE.get());
                output.accept(BCBlocks.INTEGRATION_TABLE.get());
                output.accept(BCBlocks.PROGRAMMING_TABLE.get());
                output.accept(BCBlocks.LASER.get());
                output.accept(BCBlocks.ARCHITECT.get());
                output.accept(BCBlocks.BUILDER.get());
                output.accept(BCBlocks.FILLER.get());
                output.accept(BCBlocks.LIBRARY.get());
                output.accept(BCBlocks.MARKER.get());
                output.accept(BCBlocks.ROBOT_STATION.get());
                output.accept(BCBlocks.ZONE_PLANNER.get());
            })
            .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private BCCreativeTabs() {}
}
