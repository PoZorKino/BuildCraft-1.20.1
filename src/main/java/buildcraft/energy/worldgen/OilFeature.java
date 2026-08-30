/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.worldgen;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import buildcraft.energy.fluid.BCFluids;

/**
 * Generates a BuildCraft oil deposit: an underground reservoir of oil topped by a spout that rises to
 * the surface, echoing the classic oil geysers that dot BuildCraft worlds.
 */
public class OilFeature extends Feature<NoneFeatureConfiguration> {

    public OilFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!buildcraft.config.BCConfig.oilWorldgen()) {
            return false;
        }
        WorldGenLevel level = context.level();
        BlockPos surface = context.origin();

        // Skip columns that are underwater or lack solid ground at the surface.
        BlockPos ground = surface.below();
        if (!level.getBlockState(ground).isSolid() || !level.getBlockState(surface).isAir()) {
            return false;
        }

        BlockState oil = BCFluids.OIL_BLOCK.get().defaultBlockState();
        int baseY = surface.getY() - 5;
        BlockPos center = new BlockPos(surface.getX(), baseY, surface.getZ());

        boolean placed = false;
        // Underground reservoir (a small sphere replacing stone).
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        BlockPos p = center.offset(dx, dy, dz);
                        if (level.getBlockState(p).isSolid()) {
                            level.setBlock(p, oil, 2);
                            placed = true;
                        }
                    }
                }
            }
        }
        // Spout up to the surface.
        for (int y = baseY; y <= surface.getY(); y++) {
            BlockPos p = new BlockPos(surface.getX(), y, surface.getZ());
            if (level.getBlockState(p).isSolid() || level.getBlockState(p).isAir()) {
                level.setBlock(p, oil, 2);
                placed = true;
            }
        }
        return placed;
    }
}
