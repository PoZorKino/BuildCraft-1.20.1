/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/** Common Forge config for the 1.20.1 port. Values are read after the config is loaded. */
public final class BCConfig {

    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.BooleanValue ENGINE_EXPLOSIONS;
    public static final ForgeConfigSpec.DoubleValue ENGINE_EXPLOSION_POWER;
    public static final ForgeConfigSpec.IntValue ENGINE_OVERHEAT_TICKS;
    public static final ForgeConfigSpec.BooleanValue OIL_WORLDGEN;
    public static final ForgeConfigSpec.DoubleValue ROBOT_PICKUP_RANGE;
    public static final ForgeConfigSpec.IntValue ROBOT_STATION_RANGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("engines");
        ENGINE_EXPLOSIONS = builder.comment("If true, overheated Stirling/Combustion engines explode.")
                .define("explosions", true);
        ENGINE_EXPLOSION_POWER = builder.comment("Explosion radius used when an engine overheats.")
                .defineInRange("explosionPower", 3.0, 0.0, 16.0);
        ENGINE_OVERHEAT_TICKS = builder.comment("Ticks spent at the overheat stage before exploding.")
                .defineInRange("overheatTicks", 80, 1, 1200);
        builder.pop();

        builder.push("worldgen");
        OIL_WORLDGEN = builder.comment("Generate oil deposits in the overworld.")
                .define("oil", true);
        builder.pop();

        builder.push("robotics");
        ROBOT_PICKUP_RANGE = builder.comment("How far a deployed robot looks for dropped items.")
                .defineInRange("pickupRange", 6.0, 1.0, 32.0);
        ROBOT_STATION_RANGE = builder.comment("How far a robot looks for a station to deposit items.")
                .defineInRange("stationRange", 32, 4, 128);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static boolean engineExplosions() {
        return ENGINE_EXPLOSIONS.get();
    }

    public static float explosionPower() {
        return ENGINE_EXPLOSION_POWER.get().floatValue();
    }

    public static int overheatTicks() {
        return ENGINE_OVERHEAT_TICKS.get();
    }

    public static boolean oilWorldgen() {
        return OIL_WORLDGEN.get();
    }

    public static double robotPickupRange() {
        return ROBOT_PICKUP_RANGE.get();
    }

    public static int robotStationRange() {
        return ROBOT_STATION_RANGE.get();
    }

    private BCConfig() {}
}
