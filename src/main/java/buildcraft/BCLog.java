/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/** Central logger for the ported BuildCraft mod. */
public final class BCLog {
    public static final Logger LOGGER = LogUtils.getLogger();

    private BCLog() {}
}
