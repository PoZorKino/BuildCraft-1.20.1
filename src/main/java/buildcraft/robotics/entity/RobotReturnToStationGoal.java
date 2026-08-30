/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.robotics.entity;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

import buildcraft.robotics.entity.RobotEntity.Mode;
import buildcraft.robotics.tile.TileRobotStation;

/** Fly to the nearest robot station and dump the inventory. HAUL mode only. */
public class RobotReturnToStationGoal extends Goal {

    private final RobotEntity robot;
    private TileRobotStation station;
    private int cooldown;

    public RobotReturnToStationGoal(RobotEntity robot) {
        this.robot = robot;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (robot.getMode() != Mode.HAUL || robot.isInventoryEmpty()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        station = robot.findNearestStation();
        return station != null;
    }

    @Override
    public boolean canContinueToUse() {
        return station != null && !station.isRemoved() && !robot.isInventoryEmpty();
    }

    @Override
    public void tick() {
        if (station == null) {
            return;
        }
        var pos = station.getBlockPos();
        if (robot.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 4.0) {
            robot.getNavigation().moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1.2);
        } else {
            if (!robot.depositInto(station)) {
                cooldown = 40;
                station = null;
            }
        }
    }

    @Override
    public void stop() {
        station = null;
    }
}
