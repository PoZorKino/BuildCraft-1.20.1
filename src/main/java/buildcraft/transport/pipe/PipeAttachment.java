/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.pipe;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import buildcraft.registry.BCItems;

/**
 * A single side-attachment on a pipe: a blocking plug, a coloured redstone wire, or a facade
 * that copies another block's appearance.
 */
public final class PipeAttachment {

    public enum Kind {
        PLUG, WIRE, FACADE
    }

    public final Kind kind;
    @Nullable
    public final DyeColor color;
    @Nullable
    public final String facadeBlock;

    private PipeAttachment(Kind kind, @Nullable DyeColor color, @Nullable String facadeBlock) {
        this.kind = kind;
        this.color = color;
        this.facadeBlock = facadeBlock;
    }

    public static PipeAttachment plug() {
        return new PipeAttachment(Kind.PLUG, null, null);
    }

    public static PipeAttachment wire(DyeColor color) {
        return new PipeAttachment(Kind.WIRE, color, null);
    }

    public static PipeAttachment facade(String blockId) {
        return new PipeAttachment(Kind.FACADE, null, blockId);
    }

    public boolean isBlocking() {
        return kind == Kind.PLUG || kind == Kind.FACADE;
    }

    public Block facadeAsBlock() {
        if (facadeBlock == null) {
            return Blocks.STONE;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(net.minecraft.resources.ResourceLocation.tryParse(facadeBlock));
        return block == null ? Blocks.STONE : block;
    }

    public ItemStack asItem() {
        return switch (kind) {
            case PLUG -> new ItemStack(BCItems.PIPE_PLUG.get());
            case WIRE -> {
                ItemStack stack = new ItemStack(BCItems.PIPE_WIRE.get());
                if (color != null) {
                    stack.getOrCreateTag().putString("color", color.getName());
                }
                yield stack;
            }
            case FACADE -> {
                ItemStack stack = new ItemStack(BCItems.PIPE_FACADE.get());
                if (facadeBlock != null) {
                    stack.getOrCreateTag().putString("block", facadeBlock);
                }
                yield stack;
            }
        };
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind.name());
        if (color != null) {
            tag.putString("color", color.getName());
        }
        if (facadeBlock != null) {
            tag.putString("facade", facadeBlock);
        }
        return tag;
    }

    @Nullable
    public static PipeAttachment load(CompoundTag tag) {
        try {
            Kind kind = Kind.valueOf(tag.getString("kind"));
            DyeColor color = tag.contains("color") ? DyeColor.byName(tag.getString("color"), null) : null;
            String facade = tag.contains("facade") ? tag.getString("facade") : null;
            return new PipeAttachment(kind, color, facade);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
