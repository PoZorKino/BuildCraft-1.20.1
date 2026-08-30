/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.entity;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import buildcraft.robotics.entity.RobotEntity.Mode;

/** Follow the player who programmed the robot. Green board / FOLLOW mode. */
public class RobotFollowOwnerGoal extends Goal {

    private final RobotEntity robot;

    public RobotFollowOwnerGoal(RobotEntity robot) {
        this.robot = robot;
    }

    @Override
    public boolean canUse() {
        return robot.getMode() == Mode.FOLLOW && robot.getOwner() != null;
    }

    @Override
    public void tick() {
        Player owner = robot.getOwner();
        if (owner == null) {
            return;
        }
        if (robot.distanceToSqr(owner) > 9.0) {
            robot.getNavigation().moveTo(owner.getX(), owner.getY() + 1.0, owner.getZ(), 1.3);
        }
    }
}
