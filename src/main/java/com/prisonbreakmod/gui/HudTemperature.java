package com.prisonbreakmod.gui;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.world.WeatherSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class HudTemperature {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        int temp = WeatherSystem.getInstance().getTemperature(player);
        int alertLevel = SharedPrisonState.getInstance().getAlertLevel();
        boolean blizzard = WeatherSystem.getInstance().isBlizzard();

        int screenW = mc.displayWidth / mc.gameSettings.guiScale;
        int screenH = mc.displayHeight / mc.gameSettings.guiScale;

        // Temperature bar — bottom right corner
        int barWidth = 80;
        int barHeight = 8;
        int barX = screenW - barWidth - 10;
        int barY = screenH - 60;

        // Background
        Gui.drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF000000);

        // Bar color: blue (cold) -> red (hot)
        int fillWidth = (int)(barWidth * (temp / 100.0));
        int color;
        if (temp < 20) color = 0xFF0000FF; // blue - danger
        else if (temp < 40) color = 0xFF0088FF; // light blue
        else if (temp < 60) color = 0xFF00FFFF; // cyan
        else color = 0xFFFF4400; // orange - warm

        Gui.drawRect(barX, barY, barX + fillWidth, barY + barHeight, color);

        // Label
        mc.fontRenderer.drawString("§bTemp: " + temp + "°", barX, barY - 10, 0xFFFFFF, true);

        // Alert level indicator
        if (alertLevel > 0) {
            String alertStr;
            int alertColor;
            switch (alertLevel) {
                case 1: alertStr = "§eALERT: ŻÓŁTY"; alertColor = 0xFFFF55; break;
                case 2: alertStr = "§6ALERT: POMARAŃCZOWY"; alertColor = 0xFFAA00; break;
                case 3: alertStr = "§cALERT: CZERWONY — POŚCIG"; alertColor = 0xFF5555; break;
                default: alertStr = ""; alertColor = 0xFFFFFF;
            }
            if (!alertStr.isEmpty()) {
                int alertX = screenW / 2 - mc.fontRenderer.getStringWidth(alertStr) / 2;
                mc.fontRenderer.drawString(alertStr, alertX, 5, alertColor, true);
            }
        }

        // Blizzard indicator
        if (blizzard) {
            mc.fontRenderer.drawString("§b❄ ZAMIEĆ", barX, barY - 22, 0x55FFFF, true);
        }
    }
}
