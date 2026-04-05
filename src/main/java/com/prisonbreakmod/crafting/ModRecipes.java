package com.prisonbreakmod.crafting;

import com.prisonbreakmod.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * All crafting recipes for Prison Break: Gulag.
 * Tier 1 = available immediately. Tier 2/3 = require Zbyszek.
 */
public class ModRecipes {

    public static void register() {
        registerTier1();
        registerTier2();
        registerTier3();
    }

    // =========================================================================
    // Tier 1 — Basic tools
    // =========================================================================
    private static void registerTier1() {

        // Sziv Mk1: Row1: _,_,_ / Row2: _,IronNugget,_ / Row3: IronNugget,Stick,_
        GameRegistry.addRecipe(new ItemStack(ModItems.SHIV),
                " I ", "IS ",
                'I', "nuggetIron", 'S', Items.STICK);

        // Sznur (Rope): 2×4 String grid
        GameRegistry.addRecipe(new ItemStack(ModItems.ROPE, 2),
                "SS", "SS", "SS", "SS",
                'S', Items.STRING);

        // Latarka (Flashlight): IronIngot top, GlassPane mid, Coal bot
        GameRegistry.addRecipe(new ItemStack(ModItems.FLASHLIGHT),
                " I ", " G ", " C ",
                'I', Items.IRON_INGOT, 'G', Blocks.GLASS_PANE, 'C', Items.COAL);

        // Gryps prosty (Note Simple): Paper×2
        GameRegistry.addRecipe(new ItemStack(ModItems.NOTE_SIMPLE, 4),
                "P ", "P ",
                'P', Items.PAPER);

        // Gryps zaszyfrowany (Note Coded): Paper×2 + Coal + String
        GameRegistry.addRecipe(new ItemStack(ModItems.NOTE_CODED, 2),
                "PC", "P ", " S",
                'P', Items.PAPER, 'C', Items.COAL, 'S', Items.STRING);

        // Pigułka uspokajająca (Sedative): BrownMushroom + Sugar + GlassBottle
        GameRegistry.addRecipe(new ItemStack(ModItems.SLEEPING_HERB),
                " M ", "SBS", "   ",
                'M', Blocks.BROWN_MUSHROOM, 'S', Items.SUGAR, 'B', Items.GLASS_BOTTLE);

        // Kamień Dźwiękowy (Noise Stone): Redstone+Stone+NoteBlock
        GameRegistry.addRecipe(new ItemStack(ModItems.NOISE_STONE, 2),
                " R ", "SNS", " S ",
                'R', Items.REDSTONE, 'S', Blocks.STONE, 'N', Blocks.NOTEBLOCK);

        // Ciepła kurtka (Warm Jacket): Leather+Wool
        GameRegistry.addRecipe(new ItemStack(ModItems.WARM_JACKET),
                "LWL", "LWL", " S ",
                'L', Items.LEATHER, 'W', Blocks.WOOL, 'S', Items.STRING);

        // Dziennik (Journal): Paper×4 around Leather
        GameRegistry.addRecipe(new ItemStack(ModItems.JOURNAL),
                "PPP", "PLP", "PPP",
                'P', Items.PAPER, 'L', Items.LEATHER);

        // Flara Oślepiająca (Flare): BlazePowder+Paper+RedstoneTorch
        GameRegistry.addRecipe(new ItemStack(ModItems.FLARE, 2),
                "B B", " P ", " T ",
                'B', Items.BLAZE_POWDER, 'P', Items.PAPER, 'T', Blocks.REDSTONE_TORCH);

        // Zapachowy Blok (Scent Block): RottenFlesh + FermentedSpiderEye + Redstone
        GameRegistry.addRecipe(new ItemStack(ModItems.SCENT_BLOCK),
                " R ", "RFR", " D ",
                'R', Items.ROTTEN_FLESH, 'F', Items.FERMENTED_SPIDER_EYE, 'D', Items.REDSTONE);

        // Bomba Dymna (Smoke Bomb): Gunpowder+String+BlazePowder
        GameRegistry.addRecipe(new ItemStack(ModItems.SMOKE_BOMB, 2),
                "GRG", "S S", "B B",
                'G', Items.GUNPOWDER, 'R', Blocks.GRAVEL, 'S', Items.STRING, 'B', Items.BLAZE_POWDER);
    }

    // =========================================================================
    // Tier 2 — ZbyszekCrafting handles runtime crafting; these are fallback recipes
    // =========================================================================
    private static void registerTier2() {
        // Wytrych Mk1 (Picklock Mk1): IronIngot + Redstone + String + Stick
        GameRegistry.addRecipe(new ItemStack(ModItems.PICKLOCK_MK1),
                " I ", " RS", " W ",
                'I', Items.IRON_INGOT, 'R', Items.REDSTONE, 'S', Items.STRING, 'W', Items.STICK);

        // Wytrych Mk2 (Picklock Mk2): IronIngot×2 + GoldNugget + Redstone + Lever
        GameRegistry.addRecipe(new ItemStack(ModItems.PICKLOCK_MK2),
                "I I", " G ", " RL",
                'I', Items.IRON_INGOT, 'G', "nuggetGold", 'R', Items.REDSTONE, 'L', Blocks.LEVER);

        // EMP Granulat (EMP Grenade): RedstoneBlock + GlowstoneDust + IronNugget×2
        GameRegistry.addRecipe(new ItemStack(ModItems.EMP_GRENADE),
                "R  ", "GI ", " I ",
                'R', Blocks.REDSTONE_BLOCK, 'G', Items.GLOWSTONE_DUST, 'I', "nuggetIron");

        // Hak z Liną (Grappling Hook — simple recipe, Zbyszek makes enhanced version)
        GameRegistry.addRecipe(new ItemStack(ModItems.GRAPPLING_HOOK),
                "   ", "C C", "ICI",
                'C', Items.CHAIN, 'I', Items.IRON_INGOT);

        // Fake ID — only via Zbyszek (no direct crafting recipe in survival)
        // Radio — only via Zbyszek
        // Survival Suit — only via Zbyszek
        // Master Key — only via Zbyszek with Director's key
    }

    // =========================================================================
    // Tier 3 — Endgame (Zbyszek-only, but registered for completeness)
    // =========================================================================
    private static void registerTier3() {
        // Termit (Thermite): LavaBucket + RedstoneBlock + IronBlock + BlazePowder×2
        GameRegistry.addRecipe(new ItemStack(ModItems.THERMITE),
                "B B", "RIR", "   ",
                'B', Items.BLAZE_POWDER, 'R', Blocks.REDSTONE_BLOCK, 'I', Blocks.IRON_BLOCK);
    }
}
