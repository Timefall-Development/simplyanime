package net.ghostedhype.simplyanime.client;

import net.fabricmc.api.ClientModInitializer;
import net.ghostedhype.simplyanime.client.registry.SimplyAnimeEntityRendererRegistryFabric;
import net.ghostedhype.simplyanime.client.registry.SimplyAnimeParticleRegistryFabric;

public class SimplyAnimeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SimplyAnimeClientCommon.initializeClient();
        SimplyAnimeEntityRendererRegistryFabric.register();
        SimplyAnimeParticleRegistryFabric.register();
    }
}
