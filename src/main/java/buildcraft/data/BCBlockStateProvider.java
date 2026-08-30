/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.data;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import buildcraft.BuildCraft;

public class BCBlockStateProvider extends ItemModelProvider {

    public BCBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, BuildCraft.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        basicItem(buildcraft.registry.BCItems.PAINTBRUSH.get());
        basicItem(buildcraft.registry.BCItems.PIPE_PLUG.get());
        basicItem(buildcraft.registry.BCItems.PIPE_WIRE.get());
        basicItem(buildcraft.registry.BCItems.PIPE_FACADE.get());
    }
}
