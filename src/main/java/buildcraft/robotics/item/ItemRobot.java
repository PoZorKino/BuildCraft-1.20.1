/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import buildcraft.registry.BCEntities;
import buildcraft.robotics.entity.RobotEntity;

/** The Robot item: right-click a block to deploy a flying picker robot above it. */
public class ItemRobot extends Item {

    public ItemRobot(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos spawnPos = context.getClickedPos().relative(Direction.UP);
            RobotEntity robot = BCEntities.ROBOT.get().create(serverLevel);
            if (robot != null) {
                robot.moveTo(spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 0.0F, 0.0F);
                if (context.getPlayer() != null) {
                    robot.setOwner(context.getPlayer().getUUID());
                }
                serverLevel.addFreshEntity(robot);
                ItemStack stack = context.getItemInHand();
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
