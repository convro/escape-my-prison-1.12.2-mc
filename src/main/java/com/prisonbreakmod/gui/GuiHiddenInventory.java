package com.prisonbreakmod.gui;

import com.prisonbreakmod.PrisonBreakMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hidden stash inventory — up to 9 item slots persisted in player NBT under
 * the key {@code PrisonHiddenStash}.  The stash is invisible to guards
 * (bypasses confiscation on Alert ≥ 3) but can be searched if the guard
 * directly interacts with the hiding spot.
 *
 * <p>Open with a right-click on any {@link com.prisonbreakmod.blocks.BlockDeadDrop}
 * or programmatically via {@link #open(EntityPlayer)}.
 */
@SideOnly(Side.CLIENT)
public class GuiHiddenInventory extends GuiScreen {

    private static final String NBT_KEY   = "PrisonHiddenStash";
    private static final int    SLOT_COUNT = 9;
    private static final int    SLOT_SIZE  = 18;
    private static final int    SLOT_PAD   = 4;

    /** Which slot the cursor is hovering over (-1 = none). */
    private int hoveredSlot = -1;
    /** Which slot is selected for take/place operations. */
    private int selectedSlot = -1;
    /** Cached stash contents for this render session. */
    private final List<ItemStack> stash = new ArrayList<>(SLOT_COUNT);

    private int guiLeft;
    private int guiTop;
    private int panelW;
    private int panelH;

    // -----------------------------------------------------------------------
    // Static helper — open for a player
    // -----------------------------------------------------------------------

    public static void open(EntityPlayer player) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiHiddenInventory(player));
    }

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    private final EntityPlayer player;

    public GuiHiddenInventory(EntityPlayer player) {
        super();
        this.player = player;
        loadStash();
    }

    // -----------------------------------------------------------------------
    // Layout
    // -----------------------------------------------------------------------

    @Override
    public void initGui() {
        super.initGui();
        panelW = SLOT_COUNT * (SLOT_SIZE + SLOT_PAD) + SLOT_PAD + 20;
        panelH = SLOT_SIZE + SLOT_PAD * 2 + 60;
        guiLeft = (this.width  - panelW) / 2;
        guiTop  = (this.height - panelH) / 2;

        // Close button
        this.buttonList.add(new GuiButton(0, guiLeft + panelW / 2 - 40, guiTop + panelH - 26,
                80, 20, "Zamknij (ESC)"));
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Background panel
        drawRect(guiLeft, guiTop, guiLeft + panelW, guiTop + panelH, 0xCC000000);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + panelW - 1, guiTop + panelH - 1, 0xFF1A1A1A);

        // Title
        String title = TextFormatting.GOLD + "[ Skrytka ]" + TextFormatting.RESET;
        drawCenteredString(fontRenderer, title, guiLeft + panelW / 2, guiTop + 8, 0xFFFFFF);

        // Subtitle / hint
        drawCenteredString(fontRenderer,
                TextFormatting.DARK_GRAY + "Kliknij slot aby wybrać, kliknij ponownie aby włożyć/wyjąć",
                guiLeft + panelW / 2, guiTop + 20, 0xAAAAAA);

        // Slots
        hoveredSlot = -1;
        int startX = guiLeft + SLOT_PAD + 10;
        int startY = guiTop + 32;

        for (int i = 0; i < SLOT_COUNT; i++) {
            int sx = startX + i * (SLOT_SIZE + SLOT_PAD);
            int sy = startY;

            boolean hover    = mouseX >= sx && mouseX < sx + SLOT_SIZE
                            && mouseY >= sy && mouseY < sy + SLOT_SIZE;
            boolean selected = (i == selectedSlot);

            if (hover) hoveredSlot = i;

            // Slot background
            int slotBg = selected ? 0xFF444400 : hover ? 0xFF333333 : 0xFF222222;
            drawRect(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, slotBg);
            drawRect(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFF2B2B2B);

            // Item icon (placeholder — full render requires GL)
            if (i < stash.size() && !stash.get(i).isEmpty()) {
                ItemStack stack = stash.get(i);
                GlStateManager.enableDepth();
                this.itemRender.renderItemAndEffectIntoGUI(stack, sx + 1, sy + 1);
                this.itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, sx + 1, sy + 1, null);
                GlStateManager.disableDepth();
            }

            // Slot index label (small, bottom-right)
            drawString(fontRenderer, String.valueOf(i + 1), sx + SLOT_SIZE - 5, sy + SLOT_SIZE - 7, 0x555555);
        }

        // Tooltip for hovered slot
        if (hoveredSlot >= 0 && hoveredSlot < stash.size() && !stash.get(hoveredSlot).isEmpty()) {
            ItemStack hovered = stash.get(hoveredSlot);
            List<String> tooltip = new ArrayList<>();
            tooltip.add(hovered.getDisplayName());
            tooltip.add(TextFormatting.DARK_GRAY + "Ilość: " + hovered.getCount());
            this.drawHoveringText(tooltip, mouseX, mouseY);
        }

        // Selected slot info
        if (selectedSlot >= 0) {
            String info;
            if (selectedSlot < stash.size() && !stash.get(selectedSlot).isEmpty()) {
                info = TextFormatting.YELLOW + "Wybrany: " + stash.get(selectedSlot).getDisplayName()
                     + " — kliknij RMB by wyjąć do ręki";
            } else {
                info = TextFormatting.GREEN + "Pusty slot — kliknij trzymając przedmiot by schować";
            }
            drawCenteredString(fontRenderer, info, guiLeft + panelW / 2, guiTop + panelH - 40, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // -----------------------------------------------------------------------
    // Input
    // -----------------------------------------------------------------------

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (hoveredSlot < 0) return;

        // Ensure stash list has enough slots
        while (stash.size() <= hoveredSlot) stash.add(ItemStack.EMPTY);

        if (mouseButton == 0) {
            // Left-click: select slot
            selectedSlot = (selectedSlot == hoveredSlot) ? -1 : hoveredSlot;

        } else if (mouseButton == 1) {
            // Right-click on selected: take item out into main hand
            if (hoveredSlot == selectedSlot && !stash.get(hoveredSlot).isEmpty()) {
                ItemStack toTake = stash.get(hoveredSlot);
                ItemStack currentHand = player.getHeldItemMainhand();

                if (currentHand.isEmpty()) {
                    player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, toTake);
                    stash.set(hoveredSlot, ItemStack.EMPTY);
                    saveStash();
                    selectedSlot = -1;
                }
                // If hand is occupied, swap
                else {
                    stash.set(hoveredSlot, currentHand.copy());
                    player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, toTake);
                    saveStash();
                }
            }
            // Right-click on empty slot: stash item from main hand
            else if (stash.get(hoveredSlot).isEmpty()) {
                ItemStack hand = player.getHeldItemMainhand();
                if (!hand.isEmpty()) {
                    stash.set(hoveredSlot, hand.copy());
                    player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    saveStash();
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_E) {
            this.mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // -----------------------------------------------------------------------
    // NBT persistence — stored on EntityPlayer's persistent data tag
    // -----------------------------------------------------------------------

    private void loadStash() {
        stash.clear();
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(NBT_KEY)) {
            for (int i = 0; i < SLOT_COUNT; i++) stash.add(ItemStack.EMPTY);
            return;
        }
        NBTTagList list = data.getTagList(NBT_KEY, 10 /* TAG_COMPOUND */);
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i < list.tagCount()) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                stash.add(tag.hasNoTags() ? ItemStack.EMPTY : new ItemStack(tag));
            } else {
                stash.add(ItemStack.EMPTY);
            }
        }
    }

    private void saveStash() {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : stash) {
            NBTTagCompound tag = new NBTTagCompound();
            if (!stack.isEmpty()) {
                stack.writeToNBT(tag);
            }
            list.appendTag(tag);
        }
        player.getEntityData().setTag(NBT_KEY, list);
    }

    // -----------------------------------------------------------------------
    // Static utility: check if a player has any contraband hidden
    // -----------------------------------------------------------------------

    public static boolean hasHiddenItems(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(NBT_KEY)) return false;
        NBTTagList list = data.getTagList(NBT_KEY, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            if (!list.getCompoundTagAt(i).hasNoTags()) return true;
        }
        return false;
    }

    /**
     * Confiscates all hidden items (called on Alert level 4 search event).
     * Returns number of items removed.
     */
    public static int confiscateAll(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(NBT_KEY)) return 0;
        NBTTagList list = data.getTagList(NBT_KEY, 10);
        int removed = 0;
        for (int i = 0; i < list.tagCount(); i++) {
            if (!list.getCompoundTagAt(i).hasNoTags()) removed++;
        }
        data.removeTag(NBT_KEY);
        return removed;
    }
}
