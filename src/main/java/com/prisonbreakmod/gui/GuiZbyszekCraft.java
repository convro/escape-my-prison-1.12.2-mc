package com.prisonbreakmod.gui;

import com.prisonbreakmod.crafting.ZbyszekCrafting;
import com.prisonbreakmod.events.RelationSystem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiZbyszekCraft extends GuiScreen {

    private final EntityPlayer player;
    private int selectedIndex = -1;
    private List<ZbyszekCrafting.Recipe> recipes;

    private static final int W = 320, H = 240;

    public GuiZbyszekCraft(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        recipes = ZbyszekCrafting.getAllRecipes();
        int x = (width - W) / 2, y = (height - H) / 2;

        // Recipe buttons
        for (int i = 0; i < Math.min(recipes.size(), 8); i++) {
            ZbyszekCrafting.Recipe r = recipes.get(i);
            boolean canCraft = RelationSystem.hasRelation(RelationSystem.ZBYSZEK, r.minRelation);
            String label = canCraft ? r.displayName : "§8" + r.displayName + " (rel: " + r.minRelation + ")";
            addButton(new GuiButton(i, x + 5, y + 30 + i * 22, 160, 18, label));
        }

        addButton(new GuiButton(100, x + 5, y + H - 22, 60, 18, "Craftuj"));
        addButton(new GuiButton(101, x + W - 65, y + H - 22, 60, 18, "Zamknij"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 0 && button.id < 100) {
            selectedIndex = button.id;
        }
        if (button.id == 100 && selectedIndex >= 0) {
            ZbyszekCrafting.Recipe recipe = recipes.get(selectedIndex);
            // Send craft request to server via packet
            com.prisonbreakmod.network.PrisonPacketHandler.sendZbyszekCraftRequest(recipe.id);
            mc.displayGuiScreen(null);
        }
        if (button.id == 101) mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = (width - W) / 2, y = (height - H) / 2;
        drawRect(x, y, x + W, y + H, 0xCC001100);
        drawRect(x + 1, y + 1, x + W - 1, y + 22, 0xCC003300);
        drawCenteredString(fontRenderer, "§2§lZbyszek — Warsztat Chemiczny", x + W / 2, y + 7, 0x55FF55);

        int relation = RelationSystem.getRelation(RelationSystem.ZBYSZEK);
        drawString(fontRenderer, "§7Relacja z Zbyszkiem: " + relation + "/100", x + 5, y + 24, 0x888888);

        // Recipe details panel
        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            ZbyszekCrafting.Recipe r = recipes.get(selectedIndex);
            int px = x + 170, py = y + 30;
            drawRect(px, py, x + W - 5, y + H - 25, 0xCC002200);
            drawString(fontRenderer, "§a" + r.displayName, px + 3, py + 3, 0x55FF55);
            drawString(fontRenderer, "§7Czas: " + (r.craftTimeTicksMin/1200) + "-" +
                    (r.craftTimeTicksMax/1200) + "h", px + 3, py + 15, 0xAAAAAA);
            drawString(fontRenderer, "§7Relacja: " + r.minRelation, px + 3, py + 25, 0xAAAAAA);
            drawString(fontRenderer, "§7Składniki:", px + 3, py + 38, 0xCCCCCC);
            int iy = py + 48;
            for (net.minecraft.item.ItemStack ing : r.ingredients) {
                drawString(fontRenderer, "  " + ing.getDisplayName() + " x" + ing.getCount(),
                        px + 3, iy, 0xFFFFFF);
                iy += 10;
            }
            if (r.risky) {
                drawString(fontRenderer, "§c⚠ Ryzykowny crafting!", px + 3, iy + 5, 0xFF5555);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
