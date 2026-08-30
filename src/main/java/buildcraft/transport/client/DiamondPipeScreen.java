/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.transport.menu.DiamondPipeMenu;

/** Diamond pipe filter GUI, drawn with primitives. One labelled row of filters per face. */
public class DiamondPipeScreen extends AbstractContainerScreen<DiamondPipeMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_EDGE = 0xFF373737;

    private static final int[] SIDE_COLORS = {
            0xFF555555, // DOWN
            0xFFE0E0E0, // UP
            0xFF3F76E4, // NORTH
            0xFF80C71F, // SOUTH
            0xFF3C44AA, // WEST
            0xFF835432  // EAST
    };

    public DiamondPipeScreen(DiamondPipeMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
        g.fill(x, y, x + imageWidth, y + 1, PANEL_LIGHT);
        g.fill(x, y, x + 1, y + imageHeight, PANEL_LIGHT);
        g.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, PANEL_DARK);
        g.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, PANEL_DARK);

        for (int row = 0; row < DiamondPipeMenu.FILTER_ROWS; row++) {
            int markerX = x + 168;
            int markerY = y + 18 + row * 18;
            g.fill(markerX, markerY + 1, markerX + 6, markerY + 15, SIDE_COLORS[row]);
            for (int col = 0; col < DiamondPipeMenu.FILTER_COLS; col++) {
                drawSlot(g, x + 7 + col * 18, y + 17 + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(g, x + 7 + col * 18, y + 139 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(g, x + 7 + col * 18, y + 197);
        }
    }

    private static void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, SLOT_EDGE);
        g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BG);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        for (int i = 0; i < Direction.values().length; i++) {
            if (mouseX >= leftPos + 166 && mouseX <= leftPos + 174
                    && mouseY >= topPos + 18 + i * 18 && mouseY <= topPos + 32 + i * 18) {
                g.renderTooltip(font, Component.translatable("direction.buildcraft." + Direction.values()[i].getName()),
                        mouseX, mouseY);
            }
        }
    }
}
