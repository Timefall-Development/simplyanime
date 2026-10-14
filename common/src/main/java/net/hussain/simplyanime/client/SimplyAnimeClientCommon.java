package net.hussain.simplyanime.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.ability.RhittaGrowth;
import net.hussain.simplyanime.client.pose.BladePoses;
import net.hussain.simplyanime.client.pose.RhittaPoses;
import net.hussain.simplyanime.client.renderer.*;
import net.hussain.simplyanime.config.ClientConfig;
import net.hussain.simplyanime.config.RhittaClientConfig;
import net.hussain.simplyanime.item.AnimeWeaponItem;
import net.hussain.simplyanime.item.EnumaElishItem;
import net.hussain.simplyanime.registry.EntityRegistry;
import net.hussain.simplyanime.registry.ParticlesRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.sweenus.simplyswords.client.api.SimplySwordsClientAPI;

public class SimplyAnimeClientCommon {
    public static ClientHooks clientHooks = ClientHooks.NONE;

    public void initializeClient() {
        ParticlesRegistry.init();
    }


















    //TODO Remove if not necessary
    public interface RendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    public static void init() {
        clientHooks = new CruelSunEffects();
        ClientConfig.get();
        RhittaClientConfig.get();
        SimplySwordsClientAPI.registerUniqueTooltipNamespace(Constants.MOD_ID);
    }

    //TODO Move to separate registry
    public static void registerRenderers(RendererRegistrar registrar) {
        registrar.register(EntityRegistry.HEAVEN_CHAIN_VISUAL.get(), HeavenChainVisualEntityRenderer::new);
        registrar.register(EntityRegistry.HEAVEN_MARK_VISUAL.get(), HeavenMarkVisualEntityRenderer::new);
        registrar.register(EntityRegistry.ENUMA_ELISH_VISUAL.get(), EnumaElishVisualEntityRenderer::new);
        registrar.register(EntityRegistry.CRUEL_SUN.get(), CruelSunRenderer::new);
        registrar.register(EntityRegistry.BLADE_SKILL.get(), BladeSkillRenderer::new);
    }

    //TODO Review all of the below to see if it is actually needed, and if so, where it should go. It should not
    // be in the base client class wherever possible.

    public static void tick(Minecraft client) {
        if (client.level != null) {
            RhittaPoses.tick(client.level);
            RhittaCinematics.tick(client);
            BladeCinematics.tick(client);
            holdAttack(client);
        }
    }

    private static void holdAttack(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null || client.gameMode == null || !client.options.keyAttack.isDown()
                || !ClientConfig.get().holdToAttack || !BladePoses.attacks() || player.isUsingItem() || player.isSpectator()
                || !(player.getMainHandItem().getItem() instanceof AnimeWeaponItem weapon) || weapon.attackStyle() == null
                || player.getAttackStrengthScale(0.0F) < 1.0F || BladePoses.inSkill(player) || BladePoses.aimsAtBlock(player)) {
            return;
        }
        if (client.hitResult instanceof EntityHitResult hit) {
            client.gameMode.attack(player, hit.getEntity());
        } else {
            player.resetAttackStrengthTicker();
        }
        player.swing(InteractionHand.MAIN_HAND);
    }

    public static void afterTranslucent() {
        Later.flush();
    }

    public static boolean blocksSwing() {
        Player player = Minecraft.getInstance().player;
        return player != null && player.getMainHandItem().getItem() instanceof EnumaElishItem;
    }

    public static void beforeRender(LivingEntity entity, PoseStack poseStack, float partialTick) {
        float scale = entity instanceof Player player ? RhittaGrowth.renderScale(player, partialTick) : 1.0F;
        poseStack.scale(scale, scale, scale);
        BladePoses.root(entity, poseStack, partialTick);
    }

    public static double fov(double fov, float partialTick, boolean configured) {
        return BladeCinematics.fov(RhittaCinematics.fov(fov, partialTick, configured), partialTick, configured);
    }

    public static float[] shake(float partialTick) {
        float[] sun = RhittaCinematics.shake(partialTick);
        float[] blade = BladeCinematics.shake(partialTick);
        if (sun == null || blade == null) {
            return sun == null ? blade : sun;
        }
        return new float[]{sun[0] + blade[0], sun[1] + blade[1], sun[2] + blade[2]};
    }

    public static double cameraPullBack() {
        return RhittaCinematics.cameraPullBack() + BladeCinematics.cameraPullBack();
    }

    public static void renderOverlay(GuiGraphics graphics, float partialTick) {
        RhittaCinematics.renderOverlay(graphics, partialTick);
        BladeCinematics.renderOverlay(graphics, partialTick);
    }

    public static void holdInput(Player player, Input input) {
        BladeCinematics.holdInput(player, input);
    }
}
