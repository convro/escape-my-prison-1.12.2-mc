package com.prisonbreakmod.gui;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.ai.AIResponse;
import com.prisonbreakmod.ai.DeepSeekClient;
import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.config.PrisonConfig;
import com.prisonbreakmod.entity.AbstractNPC;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SideOnly(Side.CLIENT)
public class GuiDialogue extends GuiScreen {

    private final EntityPlayer player;
    private final AbstractNPC npc;
    private final String initialMsg;

    private GuiTextField inputField;
    private final List<String> dialogueHistory = new ArrayList<>();
    private String pendingResponse = null;
    private boolean waitingForResponse = false;
    private int scrollOffset = 0;

    private static final int DIALOGUE_WIDTH = 300;
    private static final int DIALOGUE_HEIGHT = 200;

    public GuiDialogue(EntityPlayer player, AbstractNPC npc, String initialMsg) {
        this.player = player;
        this.npc = npc;
        this.initialMsg = initialMsg;
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = (width - DIALOGUE_WIDTH) / 2;
        int y = (height - DIALOGUE_HEIGHT) / 2;

        inputField = new GuiTextField(0, fontRenderer,
                x + 5, y + DIALOGUE_HEIGHT - 25, DIALOGUE_WIDTH - 10, 20);
        inputField.setMaxStringLength(256);
        inputField.setFocused(true);
        buttonList.clear();

        addButton(new net.minecraft.client.gui.GuiButton(1, x + DIALOGUE_WIDTH - 50,
                y + DIALOGUE_HEIGHT - 25, 45, 20, "Wyślij"));
        addButton(new net.minecraft.client.gui.GuiButton(2, x + 5,
                y + DIALOGUE_HEIGHT - 25, 60, 20, "Zamknij"));

        // Show NPC greeting
        String npcName = npc.getName();
        dialogueHistory.add("§7[Rozmawiasz z " + npcName + "]");
        if (npc.getLastDialogue() != null && !npc.getLastDialogue().isEmpty()) {
            dialogueHistory.add("§a" + npcName + ": §f" + npc.getLastDialogue());
        }
        if (!initialMsg.isEmpty()) {
            dialogueHistory.add("§eGracz: §f" + initialMsg);
        }
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
        if (button.id == 1) sendMessage();
        if (button.id == 2) mc.displayGuiScreen(null);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || waitingForResponse) return;

        // Sanitize input
        text = text.replaceAll("[\"'{}\\\\]", "").substring(0, Math.min(256, text.length()));
        dialogueHistory.add("§eGracz: §f" + text);
        inputField.setText("");
        waitingForResponse = true;

        final String finalText = text;
        String systemPrompt = npc.buildSystemPrompt();
        DeepSeekClient.getInstance().queryAsync(systemPrompt, finalText,
                DeepSeekClient.MODEL_CHAT, PrisonConfig.maxDialogueTokens)
                .thenAccept(response -> {
                    pendingResponse = response.getDialogue();
                    if (pendingResponse == null || pendingResponse.isEmpty()) {
                        pendingResponse = response.getReason() != null ? response.getReason() : "...";
                    }
                    waitingForResponse = false;
                    // Relation change
                    if (response.getRelChange() != 0) {
                        SharedPrisonState.getInstance().adjustRelation(npc.getNpcId(), response.getRelChange());
                    }
                    if (response.getMemory() != null && !response.getMemory().isEmpty()) {
                        npc.getMemory().addMemory(response.getMemory());
                    }
                })
                .exceptionally(ex -> {
                    pendingResponse = "§c[Błąd połączenia z AI]";
                    waitingForResponse = false;
                    return null;
                });
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (pendingResponse != null) {
            dialogueHistory.add("§a" + npc.getName() + ": §f" + pendingResponse);
            pendingResponse = null;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int x = (width - DIALOGUE_WIDTH) / 2;
        int y = (height - DIALOGUE_HEIGHT) / 2;

        // Background
        drawRect(x, y, x + DIALOGUE_WIDTH, y + DIALOGUE_HEIGHT, 0xCC000000);
        drawRect(x + 1, y + 1, x + DIALOGUE_WIDTH - 1, y + 24, 0xCC222244);

        // Title
        drawCenteredString(fontRenderer, "§l" + npc.getName(), x + DIALOGUE_WIDTH / 2, y + 7, 0xFFFFAA);

        // Relation indicator
        int rel = SharedPrisonState.getInstance().getRelation(npc.getNpcId());
        String relStr = "Relacja: " + rel + "/100";
        drawString(fontRenderer, relStr, x + 5, y + 28, 0xAAAAAA);

        // Dialogue history
        int lineHeight = 10;
        int historyAreaTop = y + 42;
        int historyAreaBottom = y + DIALOGUE_HEIGHT - 30;
        int maxLines = (historyAreaBottom - historyAreaTop) / lineHeight;

        int start = Math.max(0, dialogueHistory.size() - maxLines - scrollOffset);
        int end = Math.min(dialogueHistory.size(), start + maxLines);

        for (int i = start; i < end; i++) {
            String line = dialogueHistory.get(i);
            // Word wrap
            List<String> wrapped = fontRenderer.listFormattedStringToWidth(line, DIALOGUE_WIDTH - 10);
            for (String w : wrapped) {
                drawString(fontRenderer, w, x + 5, historyAreaTop, 0xFFFFFF);
                historyAreaTop += lineHeight;
            }
        }

        if (waitingForResponse) {
            drawCenteredString(fontRenderer, "§7Czeka na odpowiedź...",
                    x + DIALOGUE_WIDTH / 2, y + DIALOGUE_HEIGHT - 35, 0x888888);
        }

        inputField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 28) { // Enter
            sendMessage();
        } else if (keyCode == 1) { // Escape
            mc.displayGuiScreen(null);
        } else {
            inputField.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        inputField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
