package com.prisonbreakmod.gui;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.events.MissionTracker;
import com.prisonbreakmod.items.misc.ItemJournal;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiJournal extends GuiScreen {

    private final ItemStack journalStack;
    private int currentPage = 0; // 0=entries, 1=missions, 2=guards, 3=map

    private static final int W = 320, H = 220;

    public GuiJournal(ItemStack journalStack) {
        this.journalStack = journalStack;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        addButton(new net.minecraft.client.gui.GuiButton(0, x + 5, y + H - 22, 50, 18, "Notatki"));
        addButton(new net.minecraft.client.gui.GuiButton(1, x + 60, y + H - 22, 50, 18, "Misje"));
        addButton(new net.minecraft.client.gui.GuiButton(2, x + 115, y + H - 22, 80, 18, "Strażnicy"));
        addButton(new net.minecraft.client.gui.GuiButton(3, x + W - 55, y + H - 22, 50, 18, "Zamknij"));
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        switch (button.id) {
            case 0: currentPage = 0; break;
            case 1: currentPage = 1; break;
            case 2: currentPage = 2; break;
            case 3: mc.displayGuiScreen(null); break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = (width - W) / 2, y = (height - H) / 2;

        drawRect(x, y, x + W, y + H, 0xCC111100);
        drawRect(x + 1, y + 1, x + W - 1, y + 22, 0xCC332200);
        drawCenteredString(fontRenderer, "§6§lDziennik Więźnia", x + W / 2, y + 7, 0xFFFFAA);

        int contentY = y + 28;
        switch (currentPage) {
            case 0: drawEntries(x + 5, contentY); break;
            case 1: drawMissions(x + 5, contentY); break;
            case 2: drawGuards(x + 5, contentY); break;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawEntries(int x, int y) {
        List<String> entries = ItemJournal.getEntries(journalStack);
        if (entries.isEmpty()) {
            drawString(fontRenderer, "§7Brak notatek.", x, y, 0x888888);
            return;
        }
        int maxLines = 14, line = 0;
        for (int i = Math.max(0, entries.size() - maxLines); i < entries.size(); i++) {
            drawString(fontRenderer, entries.get(i), x, y + line * 10, 0xFFFFFF);
            line++;
        }
    }

    private void drawMissions(int x, int y) {
        MissionTracker tracker = MissionTracker.getInstance();
        drawString(fontRenderer, "§eFaza " + tracker.getCurrentPhase(), x, y, 0xFFFF55);
        y += 12;
        String[][] missions = {
                {"0.1", "Kontakt ze Zbyszkiem"},
                {"0.2", "Marek w stołówce"},
                {"0.3", "Waluta — 10 papierosów"},
                {"0.4", "Pierwsza przysługa"},
                {"1.1", "Harmonogram nocnych"},
                {"1.2", "Dostęp do archiwum"},
                {"1.3", "Słabość Kapitana"},
                {"1.4", "Kanalizacja"},
                {"1.5", "Odwet Pop-Kornika"},
        };
        for (String[] m : missions) {
            MissionTracker.MissionState state = tracker.getMission(m[0] + "_" + m[1].toLowerCase().replace(" ", "_"));
            String color = state == MissionTracker.MissionState.DONE ? "§a" :
                    state == MissionTracker.MissionState.ACTIVE ? "§e" :
                    state == MissionTracker.MissionState.LOCKED ? "§8" : "§c";
            drawString(fontRenderer, color + m[0] + " " + m[1], x, y, 0xFFFFFF);
            y += 10;
        }
    }

    private void drawGuards(int x, int y) {
        drawString(fontRenderer, "§bZapisane harmonogramy:", x, y, 0x55FFFF);
        y += 12;
        for (int g = 1; g <= 20; g++) {
            String sched = ItemJournal.getGuardSchedule(journalStack, g);
            if (!sched.isEmpty()) {
                List<String> lines = fontRenderer.listFormattedStringToWidth(
                        "§7S" + String.format("%02d", g) + ": " + sched, W - 15);
                for (String line : lines) {
                    drawString(fontRenderer, line, x, y, 0xCCCCCC);
                    y += 10;
                    if (y > height - 50) return;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
