package com.prisonbreakmod.blocks;

import com.prisonbreakmod.PrisonBreakMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = PrisonBreakMod.MODID)
public class ModBlocks {

    public static BlockCamera CAMERA;
    public static BlockAlarmPanel ALARM_PANEL;
    public static BlockDeadDrop DEAD_DROP;
    public static BlockLandmine LANDMINE;
    public static BlockBarredDoor BARRED_DOOR;
    public static BlockSafe SAFE;
    public static BlockIcePatch ICE_PATCH;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> reg = event.getRegistry();

        CAMERA = register(reg, new BlockCamera(), "camera");
        ALARM_PANEL = register(reg, new BlockAlarmPanel(), "alarm_panel");
        DEAD_DROP = register(reg, new BlockDeadDrop(), "dead_drop");
        LANDMINE = register(reg, new BlockLandmine(), "landmine");
        BARRED_DOOR = register(reg, new BlockBarredDoor(), "barred_door");
        SAFE = register(reg, new BlockSafe(), "safe");
        ICE_PATCH = register(reg, new BlockIcePatch(), "ice_patch");

        // Register tile entities
        GameRegistry.registerTileEntity(TileEntityCamera.class,
                PrisonBreakMod.MODID + ":camera");
        GameRegistry.registerTileEntity(TileEntityDeadDrop.class,
                PrisonBreakMod.MODID + ":dead_drop");
        GameRegistry.registerTileEntity(TileEntitySafe.class,
                PrisonBreakMod.MODID + ":safe");
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();
        registerItemBlock(reg, CAMERA);
        registerItemBlock(reg, ALARM_PANEL);
        registerItemBlock(reg, DEAD_DROP);
        registerItemBlock(reg, LANDMINE);
        registerItemBlock(reg, BARRED_DOOR);
        registerItemBlock(reg, SAFE);
        registerItemBlock(reg, ICE_PATCH);
    }

    private static <T extends Block> T register(IForgeRegistry<Block> reg, T block, String name) {
        block.setRegistryName(PrisonBreakMod.MODID, name);
        block.setUnlocalizedName(PrisonBreakMod.MODID + "." + name);
        reg.register(block);
        return block;
    }

    private static void registerItemBlock(IForgeRegistry<Item> reg, Block block) {
        ItemBlock ib = new ItemBlock(block);
        ib.setRegistryName(block.getRegistryName());
        reg.register(ib);
    }
}
