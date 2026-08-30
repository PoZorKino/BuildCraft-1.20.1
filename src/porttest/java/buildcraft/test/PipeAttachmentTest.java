/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import buildcraft.transport.pipe.PipeAttachment;

class PipeAttachmentTest {

    @Test
    void plugsAndFacadesBlockConnections() {
        assertTrue(PipeAttachment.plug().isBlocking());
        assertTrue(PipeAttachment.facade("minecraft:stone").isBlocking());
    }

    @Test
    void plugRoundTripsThroughNbt() {
        CompoundTag tag = PipeAttachment.plug().save();
        assertEquals("PLUG", tag.getString("kind"));
        PipeAttachment loaded = PipeAttachment.load(tag);
        assertNotNull(loaded);
        assertEquals(PipeAttachment.Kind.PLUG, loaded.kind);
        assertTrue(loaded.isBlocking());
    }

    @Test
    void facadeStoresCopiedBlockId() {
        PipeAttachment facade = PipeAttachment.facade("minecraft:oak_planks");
        CompoundTag tag = facade.save();
        PipeAttachment loaded = PipeAttachment.load(tag);
        assertNotNull(loaded);
        assertEquals(PipeAttachment.Kind.FACADE, loaded.kind);
        assertEquals("minecraft:oak_planks", loaded.facadeBlock);
        assertFalse(tag.contains("color"));
    }
}
