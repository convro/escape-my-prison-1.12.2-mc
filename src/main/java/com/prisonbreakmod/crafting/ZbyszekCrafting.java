package com.prisonbreakmod.crafting;

import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.entity.EntityAICompanion;
import com.prisonbreakmod.events.RelationSystem;
import com.prisonbreakmod.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NPC-based crafting system through Zbyszek.
 * Player brings ingredients to Zbyszek's cell; he crafts over time.
 */
public class ZbyszekCrafting {

    public static class Recipe {
        public final String id;
        public final String displayName;
        public final List<ItemStack> ingredients;
        public final ItemStack result;
        public final int craftTimeTicksMin; // game ticks (min)
        public final int craftTimeTicksMax;
        public final int minRelation;       // required relation with Zbyszek
        public final boolean risky;         // 5% wpadka chance

        public Recipe(String id, String displayName, List<ItemStack> ingredients,
                      ItemStack result, int minCraftH, int maxCraftH,
                      int minRelation, boolean risky) {
            this.id = id;
            this.displayName = displayName;
            this.ingredients = ingredients;
            this.result = result;
            // hours * 3600s * 20t/s / (dayLen/24) -- simplified to game ticks
            this.craftTimeTicksMin = minCraftH * 1200; // 1h game = 1200 ticks (20min day/20)
            this.craftTimeTicksMax = maxCraftH * 1200;
            this.minRelation = minRelation;
            this.risky = risky;
        }
    }

    private static final Map<String, Recipe> recipes = new HashMap<>();
    private static final Map<String, ActiveCraft> activeCrafts = new HashMap<>(); // craftId -> active

    private static class ActiveCraft {
        final String recipeId;
        final long startTick;
        final long endTick;
        final EntityPlayer player;

        ActiveCraft(String recipeId, long startTick, long endTick, EntityPlayer player) {
            this.recipeId = recipeId;
            this.startTick = startTick;
            this.endTick = endTick;
            this.player = player;
        }
    }

    static {
        registerRecipes();
    }

    private static void registerRecipes() {
        // Wytrych Mk2
        List<ItemStack> mk2Ing = new ArrayList<>();
        mk2Ing.add(new ItemStack(net.minecraft.init.Items.IRON_INGOT, 2));
        mk2Ing.add(new ItemStack(net.minecraft.init.Items.REDSTONE, 1));
        mk2Ing.add(new ItemStack(net.minecraft.init.Blocks.LEVER, 1));
        addRecipe(new Recipe("picklock_mk2", "Wytrych Mk2", mk2Ing,
                new ItemStack(ModItems.PICKLOCK_MK2), 6, 12, 60, false));

        // EMP Granulat
        List<ItemStack> empIng = new ArrayList<>();
        empIng.add(new ItemStack(net.minecraft.init.Blocks.REDSTONE_BLOCK, 1));
        empIng.add(new ItemStack(net.minecraft.init.Items.GLOWSTONE_DUST, 2));
        empIng.add(new ItemStack(net.minecraft.init.Items.IRON_NUGGET, 2));
        addRecipe(new Recipe("emp_grenade", "EMP Granulat", empIng,
                new ItemStack(ModItems.EMP_GRENADE, 2), 24, 48, 50, false));

        // Zioło Senne
        List<ItemStack> herbIng = new ArrayList<>();
        herbIng.add(new ItemStack(net.minecraft.init.Blocks.BLUE_ORCHID, 1));
        herbIng.add(new ItemStack(net.minecraft.init.Blocks.BROWN_MUSHROOM, 1));
        herbIng.add(new ItemStack(net.minecraft.init.Items.GLASS_BOTTLE, 1));
        herbIng.add(new ItemStack(net.minecraft.init.Items.SPIDER_EYE, 1));
        addRecipe(new Recipe("sleeping_herb", "Zioło Senne", herbIng,
                new ItemStack(ModItems.SLEEPING_HERB, 2), 4, 8, 40, false));

        // Fałszywe ID
        List<ItemStack> fakeIDIng = new ArrayList<>();
        fakeIDIng.add(new ItemStack(net.minecraft.init.Items.PAPER, 3));
        fakeIDIng.add(new ItemStack(net.minecraft.init.Items.INK_SACK, 1));
        fakeIDIng.add(new ItemStack(net.minecraft.init.Items.NAME_TAG, 1));
        fakeIDIng.add(new ItemStack(net.minecraft.init.Items.GOLD_NUGGET, 1));
        addRecipe(new Recipe("fake_id", "Fałszywe ID", fakeIDIng,
                new ItemStack(ModItems.FAKE_ID), 12, 24, 50, true));

        // Ciepły Kombinezon
        List<ItemStack> suitIng = new ArrayList<>();
        suitIng.add(new ItemStack(net.minecraft.init.Items.LEATHER, 4));
        suitIng.add(new ItemStack(net.minecraft.init.Blocks.WOOL, 8));
        suitIng.add(new ItemStack(net.minecraft.init.Items.RABBIT_HIDE, 4));
        addRecipe(new Recipe("survival_suit", "Ciepły Kombinezon", suitIng,
                new ItemStack(ModItems.SURVIVAL_SUIT), 6, 8, 40, false));

        // Prowiant Polarny
        List<ItemStack> rationIng = new ArrayList<>();
        rationIng.add(new ItemStack(ModItems.BREAD_RAZOWY, 3));
        rationIng.add(new ItemStack(ModItems.SOUP, 1));
        rationIng.add(new ItemStack(net.minecraft.init.Blocks.ICE, 1));
        addRecipe(new Recipe("ration", "Prowiant Polarny", rationIng,
                new ItemStack(ModItems.RATION, 4), 1, 2, 30, false));

        // Radio Więźnia
        List<ItemStack> radioIng = new ArrayList<>();
        radioIng.add(new ItemStack(net.minecraft.init.Blocks.REDSTONE_BLOCK, 2));
        radioIng.add(new ItemStack(net.minecraft.init.Items.IRON_INGOT, 4));
        radioIng.add(new ItemStack(net.minecraft.init.Items.GOLD_INGOT, 1));
        radioIng.add(new ItemStack(net.minecraft.init.Blocks.NOTEBLOCK, 1));
        addRecipe(new Recipe("radio", "Radio Więźnia", radioIng,
                new ItemStack(ModItems.RADIO), 12, 18, 50, false));

        // Hak z Liną
        List<ItemStack> hookIng = new ArrayList<>();
        hookIng.add(new ItemStack(net.minecraft.init.Blocks.IRON_BLOCK, 1));
        hookIng.add(new ItemStack(net.minecraft.init.Items.CHAIN, 4));
        hookIng.add(new ItemStack(net.minecraft.init.Items.REDSTONE, 1));
        hookIng.add(new ItemStack(net.minecraft.init.Blocks.LEVER, 1));
        addRecipe(new Recipe("grappling_hook", "Hak z Liną", hookIng,
                new ItemStack(ModItems.GRAPPLING_HOOK), 4, 6, 40, false));

        // Przebranie
        List<ItemStack> disguiseIng = new ArrayList<>();
        disguiseIng.add(new ItemStack(net.minecraft.init.Items.LEATHER, 4));
        disguiseIng.add(new ItemStack(net.minecraft.init.Items.GOLD_INGOT, 4));
        disguiseIng.add(new ItemStack(net.minecraft.init.Items.INK_SACK, 1));
        disguiseIng.add(new ItemStack(ModItems.FAKE_ID, 1));
        addRecipe(new Recipe("guard_disguise", "Przebranie Strażnika", disguiseIng,
                new ItemStack(ModItems.GUARD_DISGUISE), 8, 16, 65, true));

        // Termit
        List<ItemStack> thermiteIng = new ArrayList<>();
        thermiteIng.add(new ItemStack(net.minecraft.init.Items.LAVA_BUCKET, 1));
        thermiteIng.add(new ItemStack(net.minecraft.init.Blocks.REDSTONE_BLOCK, 1));
        thermiteIng.add(new ItemStack(net.minecraft.init.Blocks.IRON_BLOCK, 1));
        thermiteIng.add(new ItemStack(net.minecraft.init.Items.BLAZE_POWDER, 2));
        addRecipe(new Recipe("thermite", "Termit", thermiteIng,
                new ItemStack(ModItems.THERMITE), 18, 24, 60, true));
    }

    private static void addRecipe(Recipe r) { recipes.put(r.id, r); }

    public static List<Recipe> getAllRecipes() { return new ArrayList<>(recipes.values()); }

    public static Recipe getRecipe(String id) { return recipes.get(id); }

    /**
     * Start a crafting job with Zbyszek.
     * @return true if started, false if requirements not met.
     */
    public static boolean startCraft(String recipeId, EntityPlayer player, World world) {
        Recipe recipe = recipes.get(recipeId);
        if (recipe == null) return false;
        if (activeCrafts.containsKey(recipeId)) return false; // already in progress
        if (!RelationSystem.hasRelation(RelationSystem.ZBYSZEK, recipe.minRelation)) return false;

        // Consume ingredients from player inventory
        for (ItemStack ing : recipe.ingredients) {
            if (!hasIngredient(player, ing)) {
                player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§cBrakuje składnika: " + ing.getDisplayName()), true);
                return false;
            }
        }
        for (ItemStack ing : recipe.ingredients) {
            consumeIngredient(player, ing);
        }

        long now = world.getTotalWorldTime();
        long duration = recipe.craftTimeTicksMin +
                world.rand.nextInt(Math.max(1, recipe.craftTimeTicksMax - recipe.craftTimeTicksMin));
        activeCrafts.put(recipeId, new ActiveCraft(recipeId, now, now + duration, player));
        SharedPrisonState.getInstance().addEvent("ZBYSZEK_CRAFTING:" + recipeId);

        // Set Zbyszek's current project
        EntityAICompanion zbyszek = findZbyszek(world);
        if (zbyszek != null) zbyszek.setCurrentProject(recipe.displayName);

        return true;
    }

    /** Check if any active craft is done and deliver results. */
    public static void tick(World world) {
        if (world.isRemote) return;
        long now = world.getTotalWorldTime();
        List<String> done = new ArrayList<>();
        for (Map.Entry<String, ActiveCraft> e : activeCrafts.entrySet()) {
            if (now >= e.getValue().endTick) done.add(e.getKey());
        }
        for (String id : done) {
            ActiveCraft craft = activeCrafts.remove(id);
            Recipe recipe = recipes.get(id);
            if (recipe == null || craft.player == null) continue;

            // Wpadka check
            if (recipe.risky && world.rand.nextFloat() < 0.05F) {
                SharedPrisonState.getInstance().addEvent("ZBYSZEK_CRAFT_ERROR:" + id);
                craft.player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§cZbyszek miał wpadkę! Składniki stracone."), true);
            } else {
                if (!craft.player.inventory.addItemStackToInventory(recipe.result.copy())) {
                    craft.player.dropItem(recipe.result.copy(), false);
                }
                craft.player.sendStatusMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "§aZbyszek skończył: " + recipe.displayName), true);
            }

            EntityAICompanion zbyszek = findZbyszek(world);
            if (zbyszek != null) zbyszek.setCurrentProject("");
        }
    }

    public static boolean isCrafting(String recipeId) { return activeCrafts.containsKey(recipeId); }

    public static long getTimeRemaining(String recipeId, long currentTick) {
        ActiveCraft craft = activeCrafts.get(recipeId);
        if (craft == null) return -1;
        return Math.max(0, craft.endTick - currentTick);
    }

    private static boolean hasIngredient(EntityPlayer player, ItemStack ing) {
        int count = 0;
        for (ItemStack s : player.inventory.mainInventory) {
            if (!s.isEmpty() && s.getItem() == ing.getItem()) {
                count += s.getCount();
                if (count >= ing.getCount()) return true;
            }
        }
        return false;
    }

    private static void consumeIngredient(EntityPlayer player, ItemStack ing) {
        int remaining = ing.getCount();
        for (ItemStack s : player.inventory.mainInventory) {
            if (!s.isEmpty() && s.getItem() == ing.getItem()) {
                int take = Math.min(remaining, s.getCount());
                s.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }

    private static EntityAICompanion findZbyszek(World world) {
        List<EntityAICompanion> companions = world.getEntities(EntityAICompanion.class,
                e -> e.getCompanionType() == EntityAICompanion.CompanionType.ZBYSZEK);
        return companions.isEmpty() ? null : companions.get(0);
    }
}
