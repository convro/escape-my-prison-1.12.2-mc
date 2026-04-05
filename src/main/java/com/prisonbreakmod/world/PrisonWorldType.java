package com.prisonbreakmod.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PrisonWorldType extends WorldType {

    public static PrisonWorldType INSTANCE;

    public PrisonWorldType() {
        super("PrisonWorld");
    }

    public static void register() {
        INSTANCE = new PrisonWorldType();
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new PrisonChunkProvider(world, world.getSeed());
    }

    @Override
    public int getMinimumSpawnHeight(World world) {
        return 64;
    }

    @Override
    public boolean canBeCreatedOnServer() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getTranslationKey() {
        return "worldType.PrisonWorld";
    }
}
