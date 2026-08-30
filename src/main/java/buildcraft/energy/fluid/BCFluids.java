/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.fluid;

import java.util.function.Consumer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;

/** BuildCraft's iconic fluids: crude Oil and refined Fuel, with flowing/source fluids and buckets. */
public final class BCFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, BuildCraft.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, BuildCraft.MOD_ID);

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(BuildCraft.MOD_ID, path);
    }

    // --- Oil ----------------------------------------------------------------

    public static final ResourceLocation OIL_STILL = rl("fluid/oil_still");
    public static final ResourceLocation OIL_FLOW = rl("fluid/oil_flow");

    public static final RegistryObject<FluidType> OIL_TYPE = FLUID_TYPES.register("oil",
            () -> new FluidType(FluidType.Properties.create()
                    .density(900).viscosity(3000).canSwim(false).canDrown(true)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override public ResourceLocation getStillTexture() { return OIL_STILL; }
                        @Override public ResourceLocation getFlowingTexture() { return OIL_FLOW; }
                        @Override public int getTintColor() { return 0xFF1A1A1A; }
                    });
                }
            });

    public static final RegistryObject<FlowingFluid> OIL =
            FLUIDS.register("oil", () -> new ForgeFlowingFluid.Source(BCFluids.oilProps()));
    public static final RegistryObject<FlowingFluid> OIL_FLOWING =
            FLUIDS.register("oil_flowing", () -> new ForgeFlowingFluid.Flowing(BCFluids.oilProps()));

    public static final RegistryObject<LiquidBlock> OIL_BLOCK = BCBlocksHolder.BLOCKS.register("oil",
            () -> new LiquidBlock(OIL, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_BLACK)));

    public static final RegistryObject<Item> OIL_BUCKET = BCBlocksHolder.ITEMS.register("bucket_oil",
            () -> new BucketItem(OIL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private static ForgeFlowingFluid.Properties oilProps() {
        return new ForgeFlowingFluid.Properties(OIL_TYPE, OIL, OIL_FLOWING)
                .slopeFindDistance(3).levelDecreasePerBlock(2)
                .block(OIL_BLOCK).bucket(OIL_BUCKET);
    }

    // --- Fuel ---------------------------------------------------------------

    public static final ResourceLocation FUEL_STILL = rl("fluid/fuel_still");
    public static final ResourceLocation FUEL_FLOW = rl("fluid/fuel_flow");

    public static final RegistryObject<FluidType> FUEL_TYPE = FLUID_TYPES.register("fuel",
            () -> new FluidType(FluidType.Properties.create()
                    .density(750).viscosity(1000).canSwim(false).canDrown(true)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override public ResourceLocation getStillTexture() { return FUEL_STILL; }
                        @Override public ResourceLocation getFlowingTexture() { return FUEL_FLOW; }
                        @Override public int getTintColor() { return 0xFFEEDD33; }
                    });
                }
            });

    public static final RegistryObject<FlowingFluid> FUEL =
            FLUIDS.register("fuel", () -> new ForgeFlowingFluid.Source(BCFluids.fuelProps()));
    public static final RegistryObject<FlowingFluid> FUEL_FLOWING =
            FLUIDS.register("fuel_flowing", () -> new ForgeFlowingFluid.Flowing(BCFluids.fuelProps()));

    public static final RegistryObject<LiquidBlock> FUEL_BLOCK = BCBlocksHolder.BLOCKS.register("fuel",
            () -> new LiquidBlock(FUEL, BlockBehaviour.Properties.copy(Blocks.WATER)
                    .mapColor(MapColor.COLOR_YELLOW).pushReaction(PushReaction.DESTROY)));

    /** A fuel bucket that doubles as an extremely long-burning solid fuel for engines/furnaces. */
    public static final RegistryObject<Item> FUEL_BUCKET = BCBlocksHolder.ITEMS.register("bucket_fuel",
            () -> new BucketItem(FUEL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)) {
                @Override
                public int getBurnTime(net.minecraft.world.item.ItemStack itemStack,
                        net.minecraft.world.item.crafting.RecipeType<?> recipeType) {
                    return 25_600;
                }
            });

    private static ForgeFlowingFluid.Properties fuelProps() {
        return new ForgeFlowingFluid.Properties(FUEL_TYPE, FUEL, FUEL_FLOWING)
                .slopeFindDistance(4).levelDecreasePerBlock(1)
                .block(FUEL_BLOCK).bucket(FUEL_BUCKET);
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    private BCFluids() {}

    /** Small indirection so the fluids can register their block/bucket through the shared registries. */
    private static final class BCBlocksHolder {
        static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
                buildcraft.registry.BCBlocks.BLOCKS;
        static final DeferredRegister<Item> ITEMS = buildcraft.registry.BCItems.ITEMS;
    }
}
