package net.hussain.simplyanime.client;

import net.fabricmc.api.ClientModInitializer;
import net.hussain.simplyanime.SimplyAnimeCommon;

public class SimplyAnimeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SimplyAnimeCommon.initialize();
    }
}
