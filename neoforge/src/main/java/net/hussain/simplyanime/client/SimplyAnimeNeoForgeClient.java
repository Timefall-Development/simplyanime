package net.hussain.simplyanime.client;

import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.SimplyAnimeCommon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class SimplyAnimeNeoForgeClient {

    private SimplyAnimeNeoForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(SimplyAnimeCommon::initialize);

    }
}
