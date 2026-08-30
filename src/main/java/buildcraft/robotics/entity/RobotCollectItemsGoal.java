/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

import buildcraft.robotics.entity.RobotEntity.Mode;

/** Pick up nearby dropped items. Active in PICKUP and HAUL modes. */
public class RobotCollectItemsGoal extends Goal {

    private final RobotEntity robot;

    public RobotCollectItemsGoal(RobotEntity robot) {
        this.robot = robot;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Mode mode = robot.getMode();
        if (mode != Mode.PICKUP && mode != Mode.HAUL) {
            return false;
        }
        return !robot.isInventoryFull();
    }

    @Override
    public void tick() {
        robot.collectNearbyItems();
    }
}
