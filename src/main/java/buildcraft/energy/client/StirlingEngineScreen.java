/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.energy.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.energy.menu.StirlingEngineMenu;

/**
 * GUI for the Stirling Engine. Rendered entirely with primitives so it needs no bespoke texture
 * atlas entry, while still presenting the classic fuel slot + flame + heat bar layout.
 */
public class StirlingEngineScreen extends AbstractContainerScreen<StirlingEngineMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int PANEL_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int SLOT_BG = 0xFF8B8B8B;
    private static final int SLOT_EDGE = 0xFF373737;

    // Heat colours matching BuildCraft power stages (blue -> green -> yellow -> red).
    private static final int[] STAGE_COLORS = {
            0xFF4060FF, 0xFF30C030, 0xFFFFD030, 0xFFFF4020, 0xFFFF10E0
    };

    public StirlingEngineScreen(StirlingEngineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // Main panel with a simple bevelled border.
        g.fill(x, y, x + imageWidth, y + imageHeight, PANEL);
        g.fill(x, y, x + imageWidth, y + 1, PANEL_LIGHT);
        g.fill(x, y, x + 1, y + imageHeight, PANEL_LIGHT);
        g.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, PANEL_DARK);
        g.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, PANEL_DARK);

        // Fuel slot.
        drawSlot(g, x + 79, y + 52);

        // Player inventory slots.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(g, x + 7 + col * 18, y + 83 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(g, x + 7 + col * 18, y + 141);
        }

        // Flame indicator above the fuel slot.
        int flameX = x + 80;
        int flameBottom = y + 50;
        int flameH = menu.getBurnScaled(13);
        g.fill(flameX, flameBottom - 13, flameX + 14, flameBottom, SLOT_EDGE);
        if (flameH > 0) {
            g.fill(flameX + 1, flameBottom - flameH, flameX + 13, flameBottom, 0xFFFF7010);
            g.fill(flameX + 4, flameBottom - flameH, flameX + 10, flameBottom, 0xFFFFC020);
        }

        // Vertical energy / heat bar on the right.
        int barX = x + 152;
        int barTop = y + 18;
        int barH = 60;
        g.fill(barX - 1, barTop - 1, barX + 13, barTop + barH + 1, SLOT_EDGE);
        g.fill(barX, barTop, barX + 12, barTop + barH, 0xFF202020);
        int fill = menu.getEnergyScaled(barH);
        int color = STAGE_COLORS[Math.min(STAGE_COLORS.length - 1, stageFor(menu.getEnergy(), menu.getMaxEnergy()))];
        if (fill > 0) {
            g.fill(barX, barTop + (barH - fill), barX + 12, barTop + barH, color);
        }
    }

    private static int stageFor(int energy, int max) {
        double ratio = energy / (double) max;
        if (ratio < 0.25) return 0;
        if (ratio < 0.50) return 1;
        if (ratio < 0.75) return 2;
        if (ratio < 1.00) return 3;
        return 4;
    }

    private static void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, SLOT_EDGE);
        g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BG);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);

        int energy = menu.getEnergy();
        int max = menu.getMaxEnergy();
        Component energyText = Component.literal(energy + " / " + max + " FE");
        g.drawString(font, energyText, 8, 20, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
