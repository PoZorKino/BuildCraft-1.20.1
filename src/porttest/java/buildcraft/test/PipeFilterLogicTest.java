/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import buildcraft.transport.tile.PipeFilterLogic;

class PipeFilterLogicTest {

    @Test
    void emptyFiltersAllowEverything() {
        assertTrue(PipeFilterLogic.allows(false, false));
        assertTrue(PipeFilterLogic.allows(false, true));
    }

    @Test
    void populatedFiltersRequireAMatch() {
        assertTrue(PipeFilterLogic.allows(true, true));
        assertFalse(PipeFilterLogic.allows(true, false));
    }
}
