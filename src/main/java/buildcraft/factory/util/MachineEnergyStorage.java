/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.factory.util;

import net.minecraftforge.energy.EnergyStorage;

/**
 * Forge {@link EnergyStorage} for a machine that receives energy from engines/cables and spends it
 * internally to do work. External extraction is disabled.
 */
public class MachineEnergyStorage extends EnergyStorage {

    public MachineEnergyStorage(int capacity, int maxReceive) {
        super(capacity, maxReceive, 0);
    }

    /** @return true if there was enough energy and it was spent. */
    public boolean spend(int amount) {
        if (energy < amount) {
            return false;
        }
        energy -= amount;
        return true;
    }

    public void setEnergyStored(int value) {
        energy = Math.max(0, Math.min(capacity, value));
    }
}
