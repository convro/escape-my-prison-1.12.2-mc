package com.prisonbreakmod.proxy;

import com.prisonbreakmod.PrisonBreakMod;
import com.prisonbreakmod.crafting.ModRecipes;
import com.prisonbreakmod.entity.*;
import com.prisonbreakmod.entity.guards.GuardFactory;
import com.prisonbreakmod.items.projectiles.*;
import com.prisonbreakmod.network.PrisonPacketHandler;
import com.prisonbreakmod.world.PrisonWorldType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        PrisonBreakMod.LOGGER.info("[CommonProxy] preInit");
        PrisonWorldType.register();
        PrisonPacketHandler.init();
        registerEntities();
    }

    public void init(FMLInitializationEvent event) {
        PrisonBreakMod.LOGGER.info("[CommonProxy] init: registering recipes");
        ModRecipes.register();
    }

    public void postInit(FMLPostInitializationEvent event) {
        PrisonBreakMod.LOGGER.info("[CommonProxy] postInit");
    }

    protected void registerEntities() {
        int id = 1;
        // Guards
        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "guard"),
                EntityGuard.class, "guard", id++, PrisonBreakMod.instance, 64, 3, true);

        // Prisoners
        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "prisoner"),
                EntityPrisoner.class, "prisoner", id++, PrisonBreakMod.instance, 64, 3, true);

        // Companions
        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "companion"),
                EntityAICompanion.class, "companion", id++, PrisonBreakMod.instance, 64, 2, true);

        // Helicopter
        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "helicopter"),
                EntityHelicopter.class, "helicopter", id++, PrisonBreakMod.instance, 160, 5, true);

        // Tracking Dog
        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "tracking_dog"),
                EntityTrackingDog.class, "tracking_dog", id++, PrisonBreakMod.instance, 64, 3, true);

        // Projectiles
        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "bullet"),
                EntityProjectileBullet.class, "bullet", id++, PrisonBreakMod.instance, 64, 1, true);

        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "gas_cloud"),
                EntityGasCloud.class, "gas_cloud", id++, PrisonBreakMod.instance, 32, 5, true);

        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "emp_grenade"),
                EntityEMPGrenade.class, "emp_grenade", id++, PrisonBreakMod.instance, 64, 1, true);

        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "noise_stone"),
                EntityNoiseStone.class, "noise_stone", id++, PrisonBreakMod.instance, 64, 1, true);

        EntityRegistry.registerModEntity(
                new ResourceLocation(PrisonBreakMod.MODID, "flare_projectile"),
                EntityFlareProjectile.class, "flare_projectile", id++, PrisonBreakMod.instance, 64, 1, true);

        PrisonBreakMod.LOGGER.info("[CommonProxy] Registered {} entity types", id - 1);
    }
}
