/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.tile;

/**
 * Routing rule for diamond-pipe filters, kept independent of Minecraft types so it can be unit tested.
 * An empty filter row allows every item; a populated row allows only matching items.
 */
public final class PipeFilterLogic {

    private PipeFilterLogic() {}

    public static boolean allows(boolean hasAnyFilter, boolean hasMatch) {
        return !hasAnyFilter || hasMatch;
    }
}
