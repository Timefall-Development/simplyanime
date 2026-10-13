package net.hussain.simplyanime.neoforge;

import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.client.RhittaCinematics;
import net.hussain.simplyanime.client.SimplyAnimeClient;
import net.hussain.simplyanime.client.hud.CalloutHud;
import net.hussain.simplyanime.client.hud.RhittaHud;
import net.hussain.simplyanime.client.pose.FirstPerson;
import net.hussain.simplyanime.client.renderer.ArmChainLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

public class SimplyAnimeNeoForgeClient {

    static void init(IEventBus modBus) {
        SimplyAnimeClient.init();

        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                SimplyAnimeClient.registerRenderers(event::registerEntityRenderer));
        modBus.addListener((RegisterParticleProvidersEvent event) ->
                SimplyAnimeClient.registerParticles(event::registerSpriteSet));
        modBus.addListener(SimplyAnimeNeoForgeClient::onLayers);
        modBus.addListener(SimplyAnimeNeoForgeClient::onAddLayers);

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(SimplyAnimeNeoForgeClient::onClickInput);
        bus.addListener(SimplyAnimeNeoForgeClient::onRenderHand);
        bus.addListener((ClientTickEvent.Post event) -> SimplyAnimeClient.tick(Minecraft.getInstance()));
        bus.addListener(SimplyAnimeNeoForgeClient::onFov);
        bus.addListener(SimplyAnimeNeoForgeClient::onCameraAngles);
        // Pre and Post always pair up because nothing runs after LOWEST to cancel the render in between
        bus.addListener(EventPriority.LOWEST, SimplyAnimeNeoForgeClient::onRenderPre);
        bus.addListener(EventPriority.LOWEST, SimplyAnimeNeoForgeClient::onRenderPost);
    }

    private static void onLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Constants.id("callout"), (graphics, delta) ->
                CalloutHud.INSTANCE.render(graphics, delta.getGameTimeDeltaPartialTick(false)));
        event.registerAboveAll(Constants.id("rhitta_sun"), (graphics, delta) ->
                RhittaHud.INSTANCE.render(graphics, delta.getGameTimeDeltaPartialTick(false)));
        // under the rest of the HUD so the hotbar and chat stay readable through a flash
        event.registerBelowAll(Constants.id("rhitta_screen"), (graphics, delta) ->
                RhittaCinematics.renderOverlay(graphics, delta.getGameTimeDeltaPartialTick(false)));
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new ArmChainLayer(renderer, skin == PlayerSkin.Model.SLIM));
            }
        }
    }

    private static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && SimplyAnimeClient.blocksSwing()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    private static void onRenderHand(RenderHandEvent event) {
        if (FirstPerson.render(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getHand(),
                event.getItemStack(), event.getPartialTick(), event.getEquipProgress(), event.getSwingProgress())) {
            event.setCanceled(true);
        }
    }

    private static void onFov(ViewportEvent.ComputeFov event) {
        event.setFOV(RhittaCinematics.fov(event.getFOV(), (float) event.getPartialTick(), event.usedConfiguredFov()));
    }

    private static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float[] shake = RhittaCinematics.shake((float) event.getPartialTick());
        if (shake != null) {
            event.setYaw(event.getYaw() + shake[0]);
            event.setPitch(event.getPitch() + shake[1]);
            event.setRoll(event.getRoll() + shake[2]);
        }
    }

    private static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.isCanceled()) {
            return;
        }
        float scale = SimplyAnimeClient.renderScale(event.getEntity(), event.getPartialTick());
        event.getPoseStack().pushPose();
        event.getPoseStack().scale(scale, scale, scale);
    }

    private static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        event.getPoseStack().popPose();
    }
}
