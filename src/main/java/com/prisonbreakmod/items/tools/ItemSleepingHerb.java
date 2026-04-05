package com.prisonbreakmod.items.tools;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Sleeping herb — added to NPC's drink causes 4-minute sleep (4800 ticks).
 * Maximum 1 NPC sleeping at once. Logic triggered via event when NPC "drinks".
 */
public class ItemSleepingHerb extends Item {

    public ItemSleepingHerb() {
        this.setMaxStackSize(4);
    }

    /** Called by GuiDialogue or interaction event when herb is placed in drink. */
    public static void activateOnNPC(String npcId, World world) {
        SharedPrisonState state = SharedPrisonState.getInstance();
        if (state.hasSleepingHerbActive()) return; // only 1 at a time
        state.setSleepingHerbTarget(npcId, world.getTotalWorldTime() + 4800);
        state.addEvent("HERB_APPLIED:" + npcId);
    }
}
