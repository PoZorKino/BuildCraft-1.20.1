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
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.energy.block.BlockEngine;
import buildcraft.energy.tile.TileEngineCreative;
import buildcraft.energy.tile.TileEngineIron;
import buildcraft.energy.tile.TileEngineStone;
import buildcraft.energy.tile.TileEngineWood;
import buildcraft.factory.block.BlockMachine;
import buildcraft.factory.block.BlockRefinery;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileRefinery;
import buildcraft.factory.tile.TileTank;
import buildcraft.transport.block.BlockPipe;
import buildcraft.transport.tile.TilePipe;
import buildcraft.transport.tile.TilePipeWood;

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

    // --- Builders -----------------------------------------------------------

    public static final RegistryObject<Block> QUARRY = register("quarry", () -> new BlockQuarry(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)));

    // --- Transport (pipes) --------------------------------------------------

    public static final RegistryObject<Block> PIPE_COBBLESTONE = registerPipe("pipe_cobblestone",
            () -> BCBlockEntities.PIPE_COBBLESTONE.get(),
            (pos, state) -> new TilePipe(BCBlockEntities.PIPE_COBBLESTONE.get(), pos, state, 8));

    public static final RegistryObject<Block> PIPE_GOLD = registerPipe("pipe_gold",
            () -> BCBlockEntities.PIPE_GOLD.get(),
            (pos, state) -> new TilePipe(BCBlockEntities.PIPE_GOLD.get(), pos, state, 4));

    public static final RegistryObject<Block> PIPE_WOOD = registerPipe("pipe_wood",
            () -> BCBlockEntities.PIPE_WOOD.get(), TilePipeWood::new);

    private static <T extends TilePipe> RegistryObject<Block> registerPipe(String name,
            Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> type,
            BiFunction<BlockPos, net.minecraft.world.level.block.state.BlockState, T> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new BlockPipe<>(
                BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.STONE).noOcclusion(),
                type, factory));
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
