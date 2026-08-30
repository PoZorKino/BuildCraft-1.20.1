/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.data;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import buildcraft.BuildCraft;
import buildcraft.registry.BCBlocks;
import buildcraft.registry.BCItems;

public class BCLangProvider extends LanguageProvider {

    public BCLangProvider(PackOutput output) {
        super(output, BuildCraft.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.buildcraft", "BuildCraft");
        add(BCItems.PAINTBRUSH.get(), "Paintbrush");
        add(BCItems.PIPE_PLUG.get(), "Pipe Plug");
        add(BCItems.PIPE_WIRE.get(), "Pipe Wire");
        add(BCItems.PIPE_FACADE.get(), "Facade");
        add(BCBlocks.PIPE_IRON.get(), "Iron Transport Pipe");
        add(BCBlocks.PIPE_DIAMOND.get(), "Diamond Transport Pipe");
        add("item.buildcraft.paintbrush.clean", "Craft with a dye to load a colour");
        add("item.buildcraft.paintbrush.color", "Colour: %s");
        add("item.buildcraft.paintbrush.uses", "Uses: %s / %s");
        add("robot.mode.buildcraft.pickup", "Pickup");
        add("robot.mode.buildcraft.haul", "Haul to station");
        add("robot.mode.buildcraft.follow", "Follow");
        add("robot.mode.buildcraft.guard", "Guard");
        add("message.buildcraft.robot.mode", "Robot mode: %s");
    }
}
