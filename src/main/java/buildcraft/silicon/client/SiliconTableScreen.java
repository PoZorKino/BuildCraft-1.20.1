/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.silicon.menu.SiliconTableMenu;

/** GUI shared by the Integration and Programming tables, drawn with primitives. */
public class SiliconTableScreen extends AbstractContainerScreen<SiliconTableMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_EDGE = 0xFF373737;

    public SiliconTableScreen(SiliconTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
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

        if (menu.getInputCount() == 1) {
            drawSlot(g, x + 43, y + 34);
        } else {
            drawSlot(g, x + 34, y + 34);
            drawSlot(g, x + 52, y + 34);
        }
        drawSlot(g, x + 115, y + 34);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(g, x + 7 + col * 18, y + 83 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(g, x + 7 + col * 18, y + 141);
        }

        int barX = x + 152;
        int barTop = y + 18;
        int barH = 50;
        g.fill(barX - 1, barTop - 1, barX + 9, barTop + barH + 1, SLOT_EDGE);
        g.fill(barX, barTop, barX + 8, barTop + barH, 0xFF202020);
        int fill = menu.getEnergyScaled(barH);
        if (fill > 0) {
            g.fill(barX, barTop + (barH - fill), barX + 8, barTop + barH, 0xFFFF4020);
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
    }
}
