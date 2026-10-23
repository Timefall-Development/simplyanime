package net.ghostedhype.simplyanime;

import net.ghostedhype.simplyanime.networking.CalloutNetworking;
import net.ghostedhype.simplyanime.platform.Services;
import net.ghostedhype.simplyanime.registry.EntityRegistry;
import net.ghostedhype.simplyanime.registry.ItemsRegistry;
import net.ghostedhype.simplyanime.registry.SimplyAnimeCreativeModeTabRegistry;
import net.ghostedhype.simplyanime.registry.SoundRegistry;

public class SimplyAnimeCommon {
    public static void initialize() {
        //TODO Test with Better Combat. Make sure animations still work or that nothing breaks
        if (Services.PLATFORM.isModLoaded(Constants.MOD_ID)) {
            EntityRegistry.init();
            ItemsRegistry.init();
            SimplyAnimeCreativeModeTabRegistry.register();
            SoundRegistry.init();
            CalloutNetworking.init();
        }
    }
}