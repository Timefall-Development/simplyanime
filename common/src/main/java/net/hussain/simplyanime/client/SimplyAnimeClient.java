package net.hussain.simplyanime.client;

import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.SimplyAnime;
import net.hussain.simplyanime.ability.RhittaGrowth;
import net.hussain.simplyanime.client.particle.HeavenSparkParticle;
import net.hussain.simplyanime.client.particle.RuptureEmberParticle;
import net.hussain.simplyanime.client.particle.SeveredAshParticle;
import net.hussain.simplyanime.client.particle.SimpleParticleFactory;
import net.hussain.simplyanime.client.particle.SunGlowParticle;
import net.hussain.simplyanime.client.pose.RhittaPoses;
import net.hussain.simplyanime.client.renderer.CruelSunRenderer;
import net.hussain.simplyanime.client.renderer.EnumaElishVisualEntityRenderer;
import net.hussain.simplyanime.client.renderer.HeavenChainVisualEntityRenderer;
import net.hussain.simplyanime.client.renderer.HeavenMarkVisualEntityRenderer;
import net.hussain.simplyanime.config.ClientConfig;
import net.hussain.simplyanime.config.RhittaClientConfig;
import net.hussain.simplyanime.item.EnumaElishItem;
import net.hussain.simplyanime.registry.EntityRegistry;
import net.hussain.simplyanime.registry.ParticlesRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.sweenus.simplyswords.client.api.SimplySwordsClientAPI;

// client side of the mod. The loader calls these from its own events
public class SimplyAnimeClient {

    public interface RendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    public interface ParticleRegistrar {
        void register(SimpleParticleType type, ParticleEngine.SpriteParticleRegistration<SimpleParticleType> factory);
    }

    public static void init() {
        SimplyAnime.client = new CruelSunEffects();
        ClientConfig.get();
        RhittaClientConfig.get();
        SimplySwordsClientAPI.registerUniqueTooltipNamespace(Constants.MOD_ID);
    }

    public static void registerRenderers(RendererRegistrar registrar) {
        registrar.register(EntityRegistry.HEAVEN_CHAIN_VISUAL.get(), HeavenChainVisualEntityRenderer::new);
        registrar.register(EntityRegistry.HEAVEN_MARK_VISUAL.get(), HeavenMarkVisualEntityRenderer::new);
        registrar.register(EntityRegistry.ENUMA_ELISH_VISUAL.get(), EnumaElishVisualEntityRenderer::new);
        registrar.register(EntityRegistry.CRUEL_SUN.get(), CruelSunRenderer::new);
    }

    public static void registerParticles(ParticleRegistrar registrar) {
        registrar.register(ParticlesRegistry.HEAVEN_SPARK.get(), sprites -> new SimpleParticleFactory(sprites, HeavenSparkParticle::new));
        registrar.register(ParticlesRegistry.SEVERED_ASH.get(), sprites -> new SimpleParticleFactory(sprites, SeveredAshParticle::new));
        registrar.register(ParticlesRegistry.RUPTURE_EMBER.get(), sprites -> new SimpleParticleFactory(sprites, RuptureEmberParticle::new));
        registrar.register(ParticlesRegistry.SUN_SPARK.get(), sprites -> new SimpleParticleFactory(sprites, SunGlowParticle::spark));
        registrar.register(ParticlesRegistry.SUN_WISP.get(), sprites -> new SimpleParticleFactory(sprites, SunGlowParticle::wisp));
    }

    public static void tick(Minecraft client) {
        if (client.level != null) {
            RhittaPoses.tick(client.level);
            RhittaCinematics.tick(client);
        }
    }

    // no swing and no hit for Ea's left click, the attack is cancelled on the server too
    public static boolean blocksSwing() {
        Player player = Minecraft.getInstance().player;
        return player != null && player.getMainHandItem().getItem() instanceof EnumaElishItem;
    }

    // the wielder grows toward noon, drawn scaled round their feet
    public static float renderScale(Entity entity, float partialTick) {
        return entity instanceof Player player ? RhittaGrowth.renderScale(player, partialTick) : 1.0F;
    }
}
