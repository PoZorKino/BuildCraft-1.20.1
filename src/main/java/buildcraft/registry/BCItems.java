/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;

/** All standalone (non-block) items ported from BuildCraft Core. */
public final class BCItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BuildCraft.MOD_ID);

    // Gears - the backbone crafting components of BuildCraft.
    public static final RegistryObject<Item> GEAR_WOOD = simple("gear_wood");
    public static final RegistryObject<Item> GEAR_STONE = simple("gear_stone");
    public static final RegistryObject<Item> GEAR_IRON = simple("gear_iron");
    public static final RegistryObject<Item> GEAR_GOLD = simple("gear_gold");
    public static final RegistryObject<Item> GEAR_DIAMOND = simple("gear_diamond");

    // The iconic wrench, used to rotate and configure machines.
    public static final RegistryObject<Item> WRENCH =
            ITEMS.register("wrench", () -> new Item(new Item.Properties().stacksTo(1)));

    // Misc crafting components.
    public static final RegistryObject<Item> DIAMOND_SHARD = simple("diamond_shard");

    // Silicon: redstone chipsets, produced in the Assembly Table.
    public static final RegistryObject<Item> CHIPSET_REDSTONE = simple("chipset_redstone");
    public static final RegistryObject<Item> CHIPSET_IRON = simple("chipset_iron");
    public static final RegistryObject<Item> CHIPSET_GOLD = simple("chipset_gold");
    public static final RegistryObject<Item> CHIPSET_DIAMOND = simple("chipset_diamond");
    public static final RegistryObject<Item> CHIPSET_QUARTZ = simple("chipset_quartz");

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private BCItems() {}
}
