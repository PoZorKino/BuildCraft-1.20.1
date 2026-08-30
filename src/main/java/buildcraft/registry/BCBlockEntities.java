/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.builders.tile.TileArchitect;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.robotics.tile.TileRobotStation;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.silicon.tile.TileProgrammingTable;
import buildcraft.energy.tile.TileEngineCreative;
import buildcraft.energy.tile.TileEngineIron;
import buildcraft.energy.tile.TileEngineStone;
import buildcraft.energy.tile.TileEngineWood;
import buildcraft.factory.tile.TileAutoWorkbench;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller;
import buildcraft.factory.tile.TileFloodgate;
import buildcraft.factory.tile.TileHeatExchanger;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileRefinery;
import buildcraft.factory.tile.TileTank;
import buildcraft.transport.tile.TileFluidPipe;
import buildcraft.transport.tile.TileFluidPipeWood;
import buildcraft.transport.tile.TileGate;
import buildcraft.transport.tile.TilePipe;
import buildcraft.transport.tile.TilePipeWood;
import buildcraft.transport.tile.TilePowerPipe;

public final class BCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildCraft.MOD_ID);

    public static final RegistryObject<BlockEntityType<TileEngineWood>> ENGINE_WOOD =
            BLOCK_ENTITIES.register("engine_wood", () -> BlockEntityType.Builder
                    .of(TileEngineWood::new, BCBlocks.ENGINE_WOOD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEngineStone>> ENGINE_STONE =
            BLOCK_ENTITIES.register("engine_stone", () -> BlockEntityType.Builder
                    .of(TileEngineStone::new, BCBlocks.ENGINE_STONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEngineIron>> ENGINE_IRON =
            BLOCK_ENTITIES.register("engine_iron", () -> BlockEntityType.Builder
                    .of(TileEngineIron::new, BCBlocks.ENGINE_IRON.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileEngineCreative>> ENGINE_CREATIVE =
            BLOCK_ENTITIES.register("engine_creative", () -> BlockEntityType.Builder
                    .of(TileEngineCreative::new, BCBlocks.ENGINE_CREATIVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileTank>> TANK =
            BLOCK_ENTITIES.register("tank", () -> BlockEntityType.Builder
                    .of(TileTank::new, BCBlocks.TANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePump>> PUMP =
            BLOCK_ENTITIES.register("pump", () -> BlockEntityType.Builder
                    .of(TilePump::new, BCBlocks.PUMP.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileMiningWell>> MINING_WELL =
            BLOCK_ENTITIES.register("mining_well", () -> BlockEntityType.Builder
                    .of(TileMiningWell::new, BCBlocks.MINING_WELL.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileRefinery>> REFINERY =
            BLOCK_ENTITIES.register("refinery", () -> BlockEntityType.Builder
                    .of(TileRefinery::new, BCBlocks.REFINERY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileRefinery>> DISTILLER =
            BLOCK_ENTITIES.register("distiller", () -> BlockEntityType.Builder
                    .<TileRefinery>of(TileDistiller::new, BCBlocks.DISTILLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileFloodgate>> FLOODGATE =
            BLOCK_ENTITIES.register("floodgate", () -> BlockEntityType.Builder
                    .of(TileFloodgate::new, BCBlocks.FLOODGATE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileHeatExchanger>> HEAT_EXCHANGER =
            BLOCK_ENTITIES.register("heat_exchanger", () -> BlockEntityType.Builder
                    .of(TileHeatExchanger::new, BCBlocks.HEAT_EXCHANGER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileChute>> CHUTE =
            BLOCK_ENTITIES.register("chute", () -> BlockEntityType.Builder
                    .of(TileChute::new, BCBlocks.CHUTE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileAutoWorkbench>> AUTO_WORKBENCH =
            BLOCK_ENTITIES.register("auto_workbench", () -> BlockEntityType.Builder
                    .of(TileAutoWorkbench::new, BCBlocks.AUTO_WORKBENCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileQuarry>> QUARRY =
            BLOCK_ENTITIES.register("quarry", () -> BlockEntityType.Builder
                    .of(TileQuarry::new, BCBlocks.QUARRY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileAssemblyTable>> ASSEMBLY_TABLE =
            BLOCK_ENTITIES.register("assembly_table", () -> BlockEntityType.Builder
                    .of(TileAssemblyTable::new, BCBlocks.ASSEMBLY_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileLaser>> LASER =
            BLOCK_ENTITIES.register("laser", () -> BlockEntityType.Builder
                    .of(TileLaser::new, BCBlocks.LASER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileIntegrationTable>> INTEGRATION_TABLE =
            BLOCK_ENTITIES.register("integration_table", () -> BlockEntityType.Builder
                    .of(TileIntegrationTable::new, BCBlocks.INTEGRATION_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileProgrammingTable>> PROGRAMMING_TABLE =
            BLOCK_ENTITIES.register("programming_table", () -> BlockEntityType.Builder
                    .of(TileProgrammingTable::new, BCBlocks.PROGRAMMING_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileArchitect>> ARCHITECT =
            BLOCK_ENTITIES.register("architect", () -> BlockEntityType.Builder
                    .of(TileArchitect::new, BCBlocks.ARCHITECT.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileBuilder>> BUILDER =
            BLOCK_ENTITIES.register("builder", () -> BlockEntityType.Builder
                    .of(TileBuilder::new, BCBlocks.BUILDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileFiller>> FILLER =
            BLOCK_ENTITIES.register("filler", () -> BlockEntityType.Builder
                    .of(TileFiller::new, BCBlocks.FILLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileRobotStation>> ROBOT_STATION =
            BLOCK_ENTITIES.register("robot_station", () -> BlockEntityType.Builder
                    .of(TileRobotStation::new, BCBlocks.ROBOT_STATION.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePipe>> PIPE_COBBLESTONE =
            BLOCK_ENTITIES.register("pipe_cobblestone", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TilePipe(BCBlockEntities.PIPE_COBBLESTONE.get(), pos, state, 8),
                            BCBlocks.PIPE_COBBLESTONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePipe>> PIPE_GOLD =
            BLOCK_ENTITIES.register("pipe_gold", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TilePipe(BCBlockEntities.PIPE_GOLD.get(), pos, state, 4),
                            BCBlocks.PIPE_GOLD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePipeWood>> PIPE_WOOD =
            BLOCK_ENTITIES.register("pipe_wood", () -> BlockEntityType.Builder
                    .of(TilePipeWood::new, BCBlocks.PIPE_WOOD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePipe>> PIPE_STONE =
            BLOCK_ENTITIES.register("pipe_stone", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TilePipe(BCBlockEntities.PIPE_STONE.get(), pos, state, 8),
                            BCBlocks.PIPE_STONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileFluidPipe>> PIPE_FLUID_COBBLESTONE =
            BLOCK_ENTITIES.register("pipe_fluid_cobblestone", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TileFluidPipe(BCBlockEntities.PIPE_FLUID_COBBLESTONE.get(), pos, state),
                            BCBlocks.PIPE_FLUID_COBBLESTONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileFluidPipeWood>> PIPE_FLUID_WOOD =
            BLOCK_ENTITIES.register("pipe_fluid_wood", () -> BlockEntityType.Builder
                    .of(TileFluidPipeWood::new, BCBlocks.PIPE_FLUID_WOOD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePowerPipe>> PIPE_POWER_COBBLESTONE =
            BLOCK_ENTITIES.register("pipe_power_cobblestone", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TilePowerPipe(BCBlockEntities.PIPE_POWER_COBBLESTONE.get(), pos, state),
                            BCBlocks.PIPE_POWER_COBBLESTONE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TilePowerPipe>> PIPE_POWER_WOOD =
            BLOCK_ENTITIES.register("pipe_power_wood", () -> BlockEntityType.Builder
                    .of((pos, state) -> new TilePowerPipe(BCBlockEntities.PIPE_POWER_WOOD.get(), pos, state),
                            BCBlocks.PIPE_POWER_WOOD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileGate>> GATE =
            BLOCK_ENTITIES.register("gate", () -> BlockEntityType.Builder
                    .of(TileGate::new, BCBlocks.GATE.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private BCBlockEntities() {}
}
