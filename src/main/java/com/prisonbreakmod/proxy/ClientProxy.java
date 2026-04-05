package com.prisonbreakmod.proxy;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.entity.*;
import com.prisonbreakmod.items.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        PrisonBreakMod.LOGGER.info("[ClientProxy] preInit: registering renders");
        registerEntityRenders();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        registerItemModels();
    }

    private void registerEntityRenders() {
        // Use default biped/zombie renderer for humanoid NPCs as placeholders
        RenderingRegistry.registerEntityRenderingHandler(EntityGuard.class,
                manager -> new net.minecraft.client.renderer.entity.RenderZombie(manager, false));
        RenderingRegistry.registerEntityRenderingHandler(EntityPrisoner.class,
                manager -> new net.minecraft.client.renderer.entity.RenderZombie(manager, false));
        RenderingRegistry.registerEntityRenderingHandler(EntityAICompanion.class,
                manager -> new net.minecraft.client.renderer.entity.RenderZombie(manager, false));
        RenderingRegistry.registerEntityRenderingHandler(EntityHelicopter.class,
                manager -> new net.minecraft.client.renderer.entity.RenderSnowball(
                        manager, net.minecraft.init.Items.MINECART,
                        net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTrackingDog.class,
                manager -> new net.minecraft.client.renderer.entity.RenderWolf(manager));
        // Projectile renderers (use snowball as placeholder)
        RenderingRegistry.registerEntityRenderingHandler(
                com.prisonbreakmod.items.projectiles.EntityProjectileBullet.class,
                manager -> new net.minecraft.client.renderer.entity.RenderArrow(manager));
        RenderingRegistry.registerEntityRenderingHandler(
                com.prisonbreakmod.items.projectiles.EntityGasCloud.class,
                manager -> new net.minecraft.client.renderer.entity.RenderSnowball(
                        manager, net.minecraft.init.Items.SLIME_BALL,
                        net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(
                com.prisonbreakmod.items.projectiles.EntityEMPGrenade.class,
                manager -> new net.minecraft.client.renderer.entity.RenderSnowball(
                        manager, ModItems.EMP_GRENADE,
                        net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(
                com.prisonbreakmod.items.projectiles.EntityNoiseStone.class,
                manager -> new net.minecraft.client.renderer.entity.RenderSnowball(
                        manager, ModItems.NOISE_STONE,
                        net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(
                com.prisonbreakmod.items.projectiles.EntityFlareProjectile.class,
                manager -> new net.minecraft.client.renderer.entity.RenderSnowball(
                        manager, ModItems.FLARE,
                        net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
    }

    private void registerItemModels() {
        // Register item models so they render with the correct texture
        registerItemModel(ModItems.BATON, "baton");
        registerItemModel(ModItems.PISTOL_TT, "pistol_tt");
        registerItemModel(ModItems.RIFLE_MOSIN, "rifle_mosin");
        registerItemModel(ModItems.GAS_CAN, "gas_can");
        registerItemModel(ModItems.TAZER, "tazer");
        registerItemModel(ModItems.FLASHLIGHT, "flashlight");
        registerItemModel(ModItems.SHIV, "shiv");
        registerItemModel(ModItems.ROPE, "rope");
        registerItemModel(ModItems.PICKLOCK_MK1, "picklock_mk1");
        registerItemModel(ModItems.PICKLOCK_MK2, "picklock_mk2");
        registerItemModel(ModItems.EMP_GRENADE, "emp_grenade");
        registerItemModel(ModItems.SCENT_BLOCK, "scent_block");
        registerItemModel(ModItems.SLEEPING_HERB, "sleeping_herb");
        registerItemModel(ModItems.FAKE_ID, "fake_id");
        registerItemModel(ModItems.FLARE, "flare");
        registerItemModel(ModItems.NOISE_STONE, "noise_stone");
        registerItemModel(ModItems.SMOKE_BOMB, "smoke_bomb");
        registerItemModel(ModItems.GRAPPLING_HOOK, "grappling_hook");
        registerItemModel(ModItems.THERMITE, "thermite");
        registerItemModel(ModItems.RADIO, "radio");
        registerItemModel(ModItems.GUARD_DISGUISE, "guard_disguise");
        registerItemModel(ModItems.WARM_JACKET, "warm_jacket");
        registerItemModel(ModItems.SURVIVAL_SUIT, "survival_suit");
        registerItemModel(ModItems.RATION, "ration");
        registerItemModel(ModItems.MASTER_KEY, "master_key");
        registerItemModel(ModItems.CIGARETTE, "cigarette");
        registerItemModel(ModItems.NOTE_SIMPLE, "note_simple");
        registerItemModel(ModItems.NOTE_CODED, "note_coded");
        registerItemModel(ModItems.JOURNAL, "journal");
        registerItemModel(ModItems.SOUP, "soup");
        registerItemModel(ModItems.BREAD_RAZOWY, "bread_razowy");
    }

    private static void registerItemModel(Item item, String name) {
        if (item == null) return;
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(
                        new ResourceLocation(PrisonBreakMod.MODID, name), "inventory"));
    }
}
