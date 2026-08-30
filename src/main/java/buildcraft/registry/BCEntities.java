/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import buildcraft.BuildCraft;
import buildcraft.robotics.entity.RobotEntity;

public final class BCEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BuildCraft.MOD_ID);

    public static final RegistryObject<EntityType<RobotEntity>> ROBOT = ENTITIES.register("robot",
            () -> EntityType.Builder.of(RobotEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(8)
                    .build("robot"));

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        modBus.addListener(BCEntities::onAttributeCreation);
    }

    private static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ROBOT.get(), RobotEntity.createAttributes().build());
    }

    private BCEntities() {}
}
