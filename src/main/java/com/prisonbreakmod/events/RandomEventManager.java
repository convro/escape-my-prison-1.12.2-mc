package com.prisonbreakmod.events;

import com.prisonbreakmod.ai.SharedPrisonState;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Fires random prison events every 2-4 in-game hours (2400-4800 ticks).
 */
public class RandomEventManager {

    private static RandomEventManager instance;
    private long nextEventTick = 0;
    private static final int MIN_INTERVAL = 2400;
    private static final int MAX_INTERVAL = 4800;

    public static RandomEventManager getInstance() {
        if (instance == null) instance = new RandomEventManager();
        return instance;
    }

    public void tick(World world) {
        if (world.isRemote) return;
        if (world.getTotalWorldTime() < nextEventTick) return;

        Random rand = world.rand;
        nextEventTick = world.getTotalWorldTime() +
                MIN_INTERVAL + rand.nextInt(MAX_INTERVAL - MIN_INTERVAL);

        fireRandomEvent(world, rand);
    }

    private void fireRandomEvent(World world, Random rand) {
        int event = rand.nextInt(7);
        switch (event) {
            case 0: // Bójka na dziedzińcu
                SharedPrisonState.getInstance().addEvent("COURTYARD_FIGHT");
                SharedPrisonState.getInstance().addEvent("GUARDS_DISTRACTED:300");
                break;
            case 1: // Donos
                SharedPrisonState.getInstance().addEvent("INFORMER_REPORT");
                if (rand.nextFloat() < 0.4F) {
                    SharedPrisonState.getInstance().raiseAlert(1, "Informer report");
                }
                break;
            case 2: // Spisek tłumu
                SharedPrisonState.getInstance().addEvent("PRISONER_CONSPIRACY");
                break;
            case 3: // Przekazanie władzy
                SharedPrisonState.getInstance().addEvent("POWER_TRANSFER:W33");
                break;
            case 4: // Nowy więzień
                SharedPrisonState.getInstance().addEvent("NEW_PRISONER_ARRIVED");
                break;
            case 5: // Samookaleczenie
                SharedPrisonState.getInstance().addEvent("PRISONER_MEDICAL_EMERGENCY");
                SharedPrisonState.getInstance().addEvent("HOSPITAL_BUSY:1440");
                break;
            case 6: // Wizyta komisji
                SharedPrisonState.getInstance().addEvent("INSPECTION_COMMITTEE");
                break;
        }
    }
}
