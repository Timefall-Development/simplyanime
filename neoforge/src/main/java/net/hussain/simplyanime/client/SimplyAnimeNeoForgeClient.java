package net.hussain.simplyanime.client;

import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.client.registry.SimplyAnimeEntityRenderRegistryNeoForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MOD_ID, dist = {Dist.CLIENT})
public class SimplyAnimeNeoForgeClient {
    public SimplyAnimeNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(SimplyAnimeEntityRenderRegistryNeoForge::register);
    }
}
