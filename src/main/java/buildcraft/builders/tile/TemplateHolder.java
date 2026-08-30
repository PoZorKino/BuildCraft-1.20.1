/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.tile;

import net.minecraft.world.item.ItemStack;

/** A machine with a single template slot that can be loaded/unloaded by hand. */
public interface TemplateHolder {
    ItemStack getTemplate();

    void setTemplate(ItemStack stack);
}
