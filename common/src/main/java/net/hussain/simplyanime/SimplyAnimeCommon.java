package net.hussain.simplyanime;

import net.hussain.simplyanime.networking.CalloutNetworking;
import net.hussain.simplyanime.platform.Services;
import net.hussain.simplyanime.registry.EntityRegistry;
import net.hussain.simplyanime.registry.ItemsRegistry;
import net.hussain.simplyanime.registry.SoundRegistry;

public class SimplyAnimeCommon {
    public static void initialize() {
        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
            EntityRegistry.init();
            ItemsRegistry.init();
            SoundRegistry.init();
            CalloutNetworking.init();
        }
    }
}