/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.util;

import net.minecraftforge.energy.EnergyStorage;

/**
 * A Forge {@link EnergyStorage} that a generator can fill internally (via {@link #generate(int)})
 * and drain when pushing to neighbours (via {@link #consume(int)}), while still exposing a
 * read-only, extract-only view to the outside world.
 */
public class EngineEnergyStorage extends EnergyStorage {

    public EngineEnergyStorage(int capacity, int maxExtract) {
        // maxReceive = 0: outside blocks cannot push energy into an engine.
        super(capacity, 0, maxExtract);
    }

    public void generate(int amount) {
        energy = Math.min(capacity, energy + amount);
    }

    public void consume(int amount) {
        energy = Math.max(0, energy - amount);
    }

    public void setEnergyStored(int value) {
        energy = Math.max(0, Math.min(capacity, value));
    }
}
