package net.ghostedhype.simplyanime.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.ghostedhype.simplyanime.Constants;
import net.ghostedhype.simplyanime.ability.RhittaGrowth;
import net.ghostedhype.simplyanime.ability.SunPower;
import net.ghostedhype.simplyanime.config.RhittaClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RhittaHud {

    public static final RhittaHud INSTANCE = new RhittaHud();

    private static final ResourceLocation SUN = Constants.id("textures/gui/rhitta_sun.png");
    private static final int SIZE = 16;

    public void render(GuiGraphics context, float partialTick) {
        int width = context.guiWidth();
        int height = context.guiHeight();
        Minecraft client = Minecraft.getInstance();
        RhittaClientConfig cfg = RhittaClientConfig.get();
        if (!cfg.hudEnabled || client.player == null || client.level == null || client.options.hideGui
                || !RhittaGrowth.holding(client.player)) {
            return;
        }
        float power = SunPower.sunPower(client.level, client.player);
        float sun = SunPower.fraction(power);
        float time = client.player.tickCount + partialTick;
        float scale = 1.0F + 0.6F * sun + 0.04F * sun * Mth.sin(time * 0.15F);
        int box = 30;

        boolean right = cfg.hudCorner.get() == RhittaClientConfig.HudCorner.TOP_RIGHT
                || cfg.hudCorner.get() == RhittaClientConfig.HudCorner.BOTTOM_RIGHT;
        boolean bottom = cfg.hudCorner.get() == RhittaClientConfig.HudCorner.BOTTOM_LEFT
                || cfg.hudCorner.get() == RhittaClientConfig.HudCorner.BOTTOM_RIGHT;
        float cx = right ? width - cfg.hudOffsetX - box / 2.0F : cfg.hudOffsetX + box / 2.0F;
        float cy = bottom ? height - cfg.hudOffsetY - box / 2.0F - 10 : cfg.hudOffsetY + box / 2.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.45F + 0.55F * sun);
        context.pose().pushPose();
        context.pose().translate(cx, cy, 0);
        context.pose().scale(scale, scale, 1.0F);
        context.blit(SUN, -SIZE / 2, -SIZE / 2, 0, 0, SIZE, SIZE, SIZE, SIZE);
        context.pose().popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        String text = Math.round(power * 100) + "%";
        int color = sun > 0.8F ? 0xFFF97A : sun > 0.35F ? 0xEBB316 : 0x8B7B60;
        context.drawString(client.font, text, (int) cx - client.font.width(text) / 2,
                (int) cy + 14, color);
    }
}
