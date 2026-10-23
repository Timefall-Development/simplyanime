package net.ghostedhype.simplyanime;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class SimplyAnimeNeoForge {

    public SimplyAnimeNeoForge(IEventBus eventBus) {
        SimplyAnimeCommon.initialize();
    }
}