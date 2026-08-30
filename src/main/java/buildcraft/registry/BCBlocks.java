/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.builders.block.BlockBuilderMachine;
import buildcraft.builders.block.BlockLibrary;
import buildcraft.builders.block.BlockMarker;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.tile.TileArchitect;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.silicon.block.BlockAssemblyTable;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockSiliconTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileProgrammingTable;
import buildcraft.energy.block.BlockEngine;
import buildcraft.energy.tile.TileEngineCreative;
import buildcraft.energy.tile.TileEngineIron;
import buildcraft.energy.tile.TileEngineStone;
import buildcraft.energy.tile.TileEngineWood;
import buildcraft.factory.block.BlockAutoWorkbench;
import buildcraft.factory.block.BlockMachine;
import buildcraft.factory.block.BlockRefinery;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.tile.TileAutoWorkbench;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller;
import buildcraft.factory.tile.TileFloodgate;
import buildcraft.factory.tile.TileHeatExchanger;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileRefinery;
import buildcraft.factory.tile.TileTank;
import buildcraft.robotics.block.BlockRobotStation;
import buildcraft.robotics.tile.TileRobotStation;
import buildcraft.transport.block.BlockGate;
import buildcraft.transport.block.BlockPipe;
import buildcraft.transport.tile.TileFluidPipe;
import buildcraft.transport.tile.TileFluidPipeWood;
import buildcraft.transport.tile.TilePipe;
import buildcraft.transport.tile.TilePipeObsidian;
import buildcraft.transport.tile.TilePipeVoid;
import buildcraft.transport.tile.TilePipeWood;
import buildcraft.transport.tile.TilePowerPipe;

/** All blocks (and their block items) ported from BuildCraft. */
public final class BCBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BuildCraft.MOD_ID);

    public static final RegistryObject<Block> ENGINE_WOOD = registerEngine("engine_wood", MapColor.WOOD,
            () -> BCBlockEntities.ENGINE_WOOD.get(), TileEngineWood::new);

    public static final RegistryObject<Block> ENGINE_STONE = registerEngine("engine_stone", MapColor.STONE,
            () -> BCBlockEntities.ENGINE_STONE.get(), TileEngineStone::new);

    public static final RegistryObject<Block> ENGINE_IRON = registerEngine("engine_iron", MapColor.METAL,
            () -> BCBlockEntities.ENGINE_IRON.get(), TileEngineIron::new);

    public static final RegistryObject<Block> ENGINE_CREATIVE = registerEngine("engine_creative", MapColor.COLOR_PURPLE,
            () -> BCBlockEntities.ENGINE_CREATIVE.get(), TileEngineCreative::new);

    // --- Factory ------------------------------------------------------------

    public static final RegistryObject<Block> TANK = register("tank", () -> new BlockTank(
            BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.5F)
                    .sound(SoundType.GLASS).noOcclusion().isViewBlocking((s, l, p) -> false)));

    public static final RegistryObject<Block> PUMP = register("pump", () -> new BlockMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.PUMP.get(), TilePump::new));

    public static final RegistryObject<Block> MINING_WELL = register("mining_well", () -> new BlockMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F).sound(SoundType.STONE),
            () -> BCBlockEntities.MINING_WELL.get(), TileMiningWell::new));

    public static final RegistryObject<Block> REFINERY = register("refinery", () -> new BlockRefinery(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.REFINERY.get(), TileRefinery::new));

    public static final RegistryObject<Block> DISTILLER = register("distiller", () -> new BlockRefinery(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.DISTILLER.get(), TileDistiller::new));

    public static final RegistryObject<Block> FLOODGATE = register("floodgate", () -> new BlockMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.FLOODGATE.get(), TileFloodgate::new));

    public static final RegistryObject<Block> HEAT_EXCHANGER = register("heat_exchanger", () -> new BlockMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.HEAT_EXCHANGER.get(), TileHeatExchanger::new));

    public static final RegistryObject<Block> CHUTE = register("chute", () -> new BlockMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.CHUTE.get(), TileChute::new));

    public static final RegistryObject<Block> AUTO_WORKBENCH = register("auto_workbench", () -> new BlockAutoWorkbench(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD),
            () -> BCBlockEntities.AUTO_WORKBENCH.get(), TileAutoWorkbench::new));

    // --- Builders -----------------------------------------------------------

    public static final RegistryObject<Block> QUARRY = register("quarry", () -> new BlockQuarry(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)));

    public static final RegistryObject<Block> ARCHITECT = register("architect", () -> new BlockBuilderMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.ARCHITECT.get(), TileArchitect::new));

    public static final RegistryObject<Block> BUILDER = register("builder", () -> new BlockBuilderMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.BUILDER.get(), TileBuilder::new));

    public static final RegistryObject<Block> FILLER = register("filler", () -> new BlockBuilderMachine<>(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
            () -> BCBlockEntities.FILLER.get(), TileFiller::new));

    public static final RegistryObject<Block> MARKER = register("marker", () -> new BlockMarker(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.5F).noOcclusion()
                    .sound(SoundType.STONE)));

    public static final RegistryObject<Block> LIBRARY = register("library", () -> new BlockLibrary(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));

    // --- Silicon ------------------------------------------------------------

    public static final RegistryObject<Block> ASSEMBLY_TABLE = register("assembly_table",
            () -> new BlockAssemblyTable(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F).sound(SoundType.STONE),
                    () -> BCBlockEntities.ASSEMBLY_TABLE.get(), TileAssemblyTable::new));

    public static final RegistryObject<Block> LASER = register("laser", () -> new BlockLaser(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)));

    public static final RegistryObject<Block> INTEGRATION_TABLE = register("integration_table",
            () -> new BlockSiliconTable<>(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F).sound(SoundType.STONE),
                    () -> BCBlockEntities.INTEGRATION_TABLE.get(), TileIntegrationTable::new));

    public static final RegistryObject<Block> PROGRAMMING_TABLE = register("programming_table",
            () -> new BlockSiliconTable<>(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F).sound(SoundType.STONE),
                    () -> BCBlockEntities.PROGRAMMING_TABLE.get(), TileProgrammingTable::new));

    // --- Robotics -----------------------------------------------------------

    public static final RegistryObject<Block> ROBOT_STATION = register("robot_station",
            () -> new BlockRobotStation(
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL),
                    () -> BCBlockEntities.ROBOT_STATION.get(), TileRobotStation::new));

    public static final RegistryObject<Block> ZONE_PLANNER = register("zone_planner",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F)
                    .sound(SoundType.METAL)));

    // --- Transport (pipes) --------------------------------------------------

    // Item pipes.
    public static final RegistryObject<Block> PIPE_WOOD = registerPipe("pipe_wood",
            () -> BCBlockEntities.PIPE_WOOD.get(), TilePipeWood::new,
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, "item");

    public static final RegistryObject<Block> PIPE_COBBLESTONE = registerPipe("pipe_cobblestone",
            () -> BCBlockEntities.PIPE_COBBLESTONE.get(),
            (pos, state) -> new TilePipe(BCBlockEntities.PIPE_COBBLESTONE.get(), pos, state, 8),
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, "item");

    public static final RegistryObject<Block> PIPE_STONE = registerPipe("pipe_stone",
            () -> BCBlockEntities.PIPE_STONE.get(),
            (pos, state) -> new TilePipe(BCBlockEntities.PIPE_STONE.get(), pos, state, 8),
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, "item");

    public static final RegistryObject<Block> PIPE_GOLD = registerPipe("pipe_gold",
            () -> BCBlockEntities.PIPE_GOLD.get(),
            (pos, state) -> new TilePipe(BCBlockEntities.PIPE_GOLD.get(), pos, state, 4),
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, "item");

    public static final RegistryObject<Block> PIPE_OBSIDIAN = registerPipe("pipe_obsidian",
            () -> BCBlockEntities.PIPE_OBSIDIAN.get(), TilePipeObsidian::new,
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, "item");

    public static final RegistryObject<Block> PIPE_VOID = registerPipe("pipe_void",
            () -> BCBlockEntities.PIPE_VOID.get(), TilePipeVoid::new,
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, "item");

    // Fluid pipes.
    public static final RegistryObject<Block> PIPE_FLUID_WOOD = registerPipe("pipe_fluid_wood",
            () -> BCBlockEntities.PIPE_FLUID_WOOD.get(), TileFluidPipeWood::new,
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, "fluid");

    public static final RegistryObject<Block> PIPE_FLUID_COBBLESTONE = registerPipe("pipe_fluid_cobblestone",
            () -> BCBlockEntities.PIPE_FLUID_COBBLESTONE.get(),
            (pos, state) -> new TileFluidPipe(BCBlockEntities.PIPE_FLUID_COBBLESTONE.get(), pos, state),
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, "fluid");

    // Power (kinesis) pipes.
    public static final RegistryObject<Block> PIPE_POWER_WOOD = registerPipe("pipe_power_wood",
            () -> BCBlockEntities.PIPE_POWER_WOOD.get(),
            (pos, state) -> new TilePowerPipe(BCBlockEntities.PIPE_POWER_WOOD.get(), pos, state),
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY, "power");

    public static final RegistryObject<Block> PIPE_POWER_COBBLESTONE = registerPipe("pipe_power_cobblestone",
            () -> BCBlockEntities.PIPE_POWER_COBBLESTONE.get(),
            (pos, state) -> new TilePowerPipe(BCBlockEntities.PIPE_POWER_COBBLESTONE.get(), pos, state),
            () -> net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY, "power");

    public static final RegistryObject<Block> GATE = register("gate", () -> new BlockGate(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0F).sound(SoundType.METAL)));

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> RegistryObject<Block> registerPipe(
            String name,
            Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> type,
            BiFunction<BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory,
            Supplier<net.minecraftforge.common.capabilities.Capability<?>> connectCap, String kind) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new BlockPipe<>(
                BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.STONE).noOcclusion(),
                type, factory, connectCap, kind));
        BCItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static RegistryObject<Block> register(String name, Supplier<Block> block) {
        RegistryObject<Block> obj = BLOCKS.register(name, block);
        BCItems.ITEMS.register(name, () -> new BlockItem(obj.get(), new Item.Properties()));
        return obj;
    }

    private static <T extends buildcraft.energy.tile.TileEngineBase> RegistryObject<Block> registerEngine(
            String name, MapColor color,
            Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> type,
            java.util.function.BiFunction<net.minecraft.core.BlockPos,
                    net.minecraft.world.level.block.state.BlockState, T> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new BlockEngine<>(
                BlockBehaviour.Properties.of().mapColor(color).strength(2.5F).sound(SoundType.STONE),
                type, factory));
        BCItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    private BCBlocks() {}
}
