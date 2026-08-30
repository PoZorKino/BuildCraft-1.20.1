/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.energy.menu.EngineMenu;

public final class BCMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, BuildCraft.MOD_ID);

    public static final RegistryObject<MenuType<EngineMenu>> ENGINE =
            MENUS.register("engine", () -> IForgeMenuType.create(EngineMenu::new));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    private BCMenuTypes() {}
}
