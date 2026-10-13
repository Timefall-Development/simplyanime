package net.hussain.simplyanime.forge;

import net.hussain.simplyanime.client.RhittaCinematics;
import net.hussain.simplyanime.client.SimplyAnimeClient;
import net.hussain.simplyanime.client.hud.CalloutHud;
import net.hussain.simplyanime.client.hud.RhittaHud;
import net.hussain.simplyanime.client.pose.FirstPerson;
import net.hussain.simplyanime.client.renderer.ArmChainLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

public class SimplyAnimeForgeClient {

    static void init(IEventBus modBus) {
        SimplyAnimeClient.init();

        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                SimplyAnimeClient.registerRenderers(event::registerEntityRenderer));
        modBus.addListener((RegisterParticleProvidersEvent event) ->
                SimplyAnimeClient.registerParticles(event::registerSpriteSet));
        modBus.addListener(SimplyAnimeForgeClient::onOverlays);
        modBus.addListener(SimplyAnimeForgeClient::onAddLayers);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(SimplyAnimeForgeClient::onClickInput);
        bus.addListener(SimplyAnimeForgeClient::onRenderHand);
        bus.addListener(SimplyAnimeForgeClient::onClientTick);
        bus.addListener(SimplyAnimeForgeClient::onFov);
        bus.addListener(SimplyAnimeForgeClient::onCameraAngles);
        // Pre and Post always pair up because nothing runs after LOWEST to cancel the render in between
        bus.addListener(EventPriority.LOWEST, SimplyAnimeForgeClient::onRenderPre);
        bus.addListener(EventPriority.LOWEST, SimplyAnimeForgeClient::onRenderPost);
    }

    private static void onOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("callout", (gui, graphics, partialTick, width, height) ->
                CalloutHud.INSTANCE.render(graphics, partialTick));
        event.registerAboveAll("rhitta_sun", (gui, graphics, partialTick, width, height) ->
                RhittaHud.INSTANCE.render(graphics, partialTick));
        // under the rest of the HUD so the hotbar and chat stay readable through a flash
        event.registerBelowAll("rhitta_screen", (gui, graphics, partialTick, width, height) ->
                RhittaCinematics.renderOverlay(graphics, partialTick));
    }

    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new ArmChainLayer(renderer, skin.equals("slim")));
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

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            SimplyAnimeClient.tick(Minecraft.getInstance());
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
