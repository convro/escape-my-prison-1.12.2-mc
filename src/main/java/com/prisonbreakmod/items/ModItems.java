package com.prisonbreakmod.items;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.items.misc.*;
import com.prisonbreakmod.items.survival.*;
import com.prisonbreakmod.items.tools.*;
import com.prisonbreakmod.items.weapons.*;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID)
public class ModItems {

    // Weapons
    public static ItemBaton BATON;
    public static ItemPistolTT PISTOL_TT;
    public static ItemRifleMosin RIFLE_MOSIN;
    public static ItemGasCan GAS_CAN;
    public static ItemTazer TAZER;
    public static ItemFlashlight FLASHLIGHT;

    // Tools
    public static ItemShiv SHIV;
    public static ItemRope ROPE;
    public static ItemPicklock PICKLOCK_MK1;
    public static ItemPicklock PICKLOCK_MK2;
    public static ItemEMPGrenade EMP_GRENADE;
    public static ItemScentBlock SCENT_BLOCK;
    public static ItemSleepingHerb SLEEPING_HERB;
    public static ItemFakeID FAKE_ID;
    public static ItemFlare FLARE;
    public static ItemNoiseStone NOISE_STONE;
    public static ItemSmokeBomb SMOKE_BOMB;
    public static ItemGrapplingHook GRAPPLING_HOOK;
    public static ItemThermite THERMITE;
    public static ItemRadio RADIO;
    public static ItemGuardDisguise GUARD_DISGUISE;

    // Survival
    public static ItemWarmJacket WARM_JACKET;
    public static ItemSurvivalSuit SURVIVAL_SUIT;
    public static ItemRation RATION;
    public static ItemMasterKey MASTER_KEY;

    // Misc
    public static ItemCigarette CIGARETTE;
    public static ItemNoteSimple NOTE_SIMPLE;
    public static ItemNoteCoded NOTE_CODED;
    public static ItemJournal JOURNAL;
    public static ItemSoup SOUP;
    public static ItemBreadRazowy BREAD_RAZOWY;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();

        BATON = register(reg, new ItemBaton(), "baton");
        PISTOL_TT = register(reg, new ItemPistolTT(), "pistol_tt");
        RIFLE_MOSIN = register(reg, new ItemRifleMosin(), "rifle_mosin");
        GAS_CAN = register(reg, new ItemGasCan(), "gas_can");
        TAZER = register(reg, new ItemTazer(), "tazer");
        FLASHLIGHT = register(reg, new ItemFlashlight(), "flashlight");

        SHIV = register(reg, new ItemShiv(), "shiv");
        ROPE = register(reg, new ItemRope(), "rope");
        PICKLOCK_MK1 = register(reg, new ItemPicklock(1), "picklock_mk1");
        PICKLOCK_MK2 = register(reg, new ItemPicklock(2), "picklock_mk2");
        EMP_GRENADE = register(reg, new ItemEMPGrenade(), "emp_grenade");
        SCENT_BLOCK = register(reg, new ItemScentBlock(), "scent_block");
        SLEEPING_HERB = register(reg, new ItemSleepingHerb(), "sleeping_herb");
        FAKE_ID = register(reg, new ItemFakeID(), "fake_id");
        FLARE = register(reg, new ItemFlare(), "flare");
        NOISE_STONE = register(reg, new ItemNoiseStone(), "noise_stone");
        SMOKE_BOMB = register(reg, new ItemSmokeBomb(), "smoke_bomb");
        GRAPPLING_HOOK = register(reg, new ItemGrapplingHook(), "grappling_hook");
        THERMITE = register(reg, new ItemThermite(), "thermite");
        RADIO = register(reg, new ItemRadio(), "radio");
        GUARD_DISGUISE = register(reg, new ItemGuardDisguise(), "guard_disguise");

        WARM_JACKET = register(reg, new ItemWarmJacket(), "warm_jacket");
        SURVIVAL_SUIT = register(reg, new ItemSurvivalSuit(), "survival_suit");
        RATION = register(reg, new ItemRation(), "ration");
        MASTER_KEY = register(reg, new ItemMasterKey(), "master_key");

        CIGARETTE = register(reg, new ItemCigarette(), "cigarette");
        NOTE_SIMPLE = register(reg, new ItemNoteSimple(), "note_simple");
        NOTE_CODED = register(reg, new ItemNoteCoded(), "note_coded");
        JOURNAL = register(reg, new ItemJournal(), "journal");
        SOUP = register(reg, new ItemSoup(), "soup");
        BREAD_RAZOWY = register(reg, new ItemBreadRazowy(), "bread_razowy");
    }

    private static <T extends Item> T register(IForgeRegistry<Item> reg, T item, String name) {
        item.setRegistryName(PrisonBreakMod.MODID, name);
        item.setUnlocalizedName(PrisonBreakMod.MODID + "." + name);
        reg.register(item);
        return item;
    }
}
