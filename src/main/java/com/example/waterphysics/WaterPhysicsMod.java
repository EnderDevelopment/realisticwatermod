package com.example.waterphysics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class WaterPhysicsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.END_LEVEL_TICK.register(WaterReactions::tick);
    }
}
