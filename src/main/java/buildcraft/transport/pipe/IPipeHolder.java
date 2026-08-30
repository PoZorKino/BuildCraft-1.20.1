/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.pipe;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shared pipe-side state for every transport pipe kind (items, fluids, kinesis). Attachments,
 * colour, and wire conduction live here so plugs/wires/facades and the paintbrush work on all
 * pipes, not only item pipes.
 */
public interface IPipeHolder {

    BlockEntity asBlockEntity();

    PipeSideState sides();

    @Nullable
    default PipeAttachment getAttachment(Direction side) {
        return sides().get(side);
    }

    default Map<Direction, PipeAttachment> getAttachments() {
        return sides().getAttachments();
    }

    default boolean isSideBlocked(Direction side) {
        return sides().isSideBlocked(side);
    }

    default boolean hasWire() {
        return sides().hasWire();
    }

    default boolean hasWire(DyeColor color) {
        return sides().hasWire(color);
    }

    default boolean isWirePowered() {
        return sides().isWirePowered();
    }

    @Nullable
    default DyeColor getColor() {
        return sides().getColor();
    }

    default boolean setColor(@Nullable DyeColor color) {
        return sides().setColor(color);
    }

    default boolean attach(Direction side, PipeAttachment attachment) {
        return sides().attach(side, attachment);
    }

    @Nullable
    default PipeAttachment removeAttachment(Direction side) {
        return sides().remove(side);
    }

    default void dropAttachments(Level level, BlockPos pos) {
        sides().dropAll(level, pos);
    }

    default void updateWirePower() {
        sides().updateWirePower();
    }
}
