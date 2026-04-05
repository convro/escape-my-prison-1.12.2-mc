package com.prisonbreakmod;

import com.prisonbreakmod.ai.AsyncAIHandler;
import com.prisonbreakmod.ai.SharedPrisonState;
import com.prisonbreakmod.config.PrisonConfig;
import com.prisonbreakmod.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
    modid   = PrisonBreakMod.MODID,
    name    = PrisonBreakMod.NAME,
    version = PrisonBreakMod.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    guiFactory = "com.prisonbreakmod.config.PrisonConfigGuiFactory"
)
public class PrisonBreakMod {

    public static final String MODID   = "prisonbreakmod";
    public static final String NAME    = "Prison Break: Gulag";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.prisonbreakmod.proxy.ClientProxy",
        serverSide = "com.prisonbreakmod.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static PrisonBreakMod instance;

    // Singleton AI handler accessible throughout the mod
    private AsyncAIHandler aiHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("[{}] Pre-initialization started.", NAME);

        // Initialize config via @Config — Forge registers it automatically,
        // but we trigger a manual refresh here so defaults propagate.
        net.minecraftforge.common.config.ConfigManager.sync(MODID, net.minecraftforge.common.config.Config.Type.INSTANCE);

        proxy.preInit(event);

        LOGGER.info("[{}] Pre-initialization complete.", NAME);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("[{}] Initialization started.", NAME);

        proxy.init(event);

        // Boot AI subsystem after config is ready
        aiHandler = new AsyncAIHandler();
        SharedPrisonState.getInstance().reset();

        LOGGER.info("[{}] AI subsystem started.", NAME);
        LOGGER.info("[{}] Initialization complete.", NAME);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("[{}] Post-initialization started.", NAME);

        proxy.postInit(event);

        LOGGER.info("[{}] Post-initialization complete.", NAME);
    }

    /** Returns the mod-wide AsyncAIHandler instance. */
    public AsyncAIHandler getAIHandler() {
        return aiHandler;
    }

    /** Convenience static accessor. */
    public static PrisonBreakMod getInstance() {
        return instance;
    }
}
