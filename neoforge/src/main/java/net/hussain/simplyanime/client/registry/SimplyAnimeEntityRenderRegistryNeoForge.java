package net.hussain.simplyanime.client.registry;

import net.hussain.simplyanime.client.renderer.*;
import net.hussain.simplyanime.registry.EntityRegistry;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class SimplyAnimeEntityRenderRegistryNeoForge {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.HEAVEN_CHAIN_VISUAL.get(), HeavenChainVisualEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HEAVEN_MARK_VISUAL.get(), HeavenMarkVisualEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ENUMA_ELISH_VISUAL.get(), EnumaElishVisualEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CRUEL_SUN.get(), CruelSunRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLADE_SKILL.get(), BladeSkillRenderer::new);
    }
}
