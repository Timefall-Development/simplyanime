package net.ghostedhype.simplyanime;

import net.fabricmc.api.ModInitializer;

public class SimplyAnimeFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SimplyAnimeCommon.initialize();
    }
}