package net.ghostedhype.simplyanime.client;

import net.ghostedhype.simplyanime.Constants;
import net.ghostedhype.simplyanime.client.registry.SimplyAnimeEntityRenderRegistryNeoForge;
import net.ghostedhype.simplyanime.client.registry.SimplyAnimeParticleRegistryNeoForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MOD_ID, dist = {Dist.CLIENT})
public class SimplyAnimeNeoForgeClient {
    public SimplyAnimeNeoForgeClient(IEventBus modEventBus) {
        SimplyAnimeClientCommon.initializeClient();
        modEventBus.addListener(SimplyAnimeParticleRegistryNeoForge::register);
        modEventBus.addListener(SimplyAnimeEntityRenderRegistryNeoForge::register);
    }
}
