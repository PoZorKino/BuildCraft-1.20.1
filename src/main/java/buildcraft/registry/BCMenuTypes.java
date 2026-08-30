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
import buildcraft.factory.menu.AutoWorkbenchMenu;
import buildcraft.silicon.menu.AssemblyMenu;
import buildcraft.silicon.menu.SiliconTableMenu;
import buildcraft.transport.menu.DiamondPipeMenu;

public final class BCMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, BuildCraft.MOD_ID);

    public static final RegistryObject<MenuType<EngineMenu>> ENGINE =
            MENUS.register("engine", () -> IForgeMenuType.create(EngineMenu::new));

    public static final RegistryObject<MenuType<AssemblyMenu>> ASSEMBLY_TABLE =
            MENUS.register("assembly_table", () -> IForgeMenuType.create(AssemblyMenu::new));

    public static final RegistryObject<MenuType<AutoWorkbenchMenu>> AUTO_WORKBENCH =
            MENUS.register("auto_workbench", () -> IForgeMenuType.create(AutoWorkbenchMenu::new));

    public static final RegistryObject<MenuType<SiliconTableMenu>> INTEGRATION_TABLE =
            MENUS.register("integration_table", () -> IForgeMenuType.create(SiliconTableMenu::integration));

    public static final RegistryObject<MenuType<SiliconTableMenu>> PROGRAMMING_TABLE =
            MENUS.register("programming_table", () -> IForgeMenuType.create(SiliconTableMenu::programming));

    public static final RegistryObject<MenuType<DiamondPipeMenu>> PIPE_DIAMOND =
            MENUS.register("pipe_diamond", () -> IForgeMenuType.create(DiamondPipeMenu::new));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    private BCMenuTypes() {}
}
