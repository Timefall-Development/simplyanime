package net.hussain.simplyanime.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.hussain.simplyanime.callout.Callout;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

// ability name above the hotbar, small on purpose
public class CalloutHud {

    public static final CalloutHud INSTANCE = new CalloutHud();

    private static final float DURATION = 56.0F;
    private static final float POP = 5.0F;
    private static final float FADE = 14.0F;
    private static final Style STYLE = Style.EMPTY.withBold(true).withItalic(true);

    private Callout current;
    private long startedAt;

    public void show(Callout callout) {
        this.current = callout;
        this.startedAt = Util.getMillis();
    }

    public void render(GuiGraphics context, float partialTick) {
        int width = context.guiWidth();
        int height = context.guiHeight();
        Minecraft client = Minecraft.getInstance();
        if (this.current == null || client.options.hideGui) {
            return;
        }
        float age = (Util.getMillis() - this.startedAt) / 50.0F;
        if (age >= DURATION) {
            this.current = null;
            return;
        }
        float alpha = Math.min(1.0F, age / 3.0F) * Math.min(1.0F, (DURATION - age) / FADE);
        // overshoot then settle
        float pop = 1.0F - Math.min(1.0F, age / POP);
        float scale = 1.15F + 0.5F * pop * pop;

        Font font = client.font;
        String text = I18n.get(this.current.key).toUpperCase();
        int textWidth = font.width(Component.literal(text).setStyle(STYLE)) + (text.length() - 1);

        RenderSystem.enableBlend();
        context.pose().pushPose();
        // in front of the chat, fresh chat lines used to hide it
        context.pose().translate(width / 2.0F, height - 74.0F, 200.0F);
        context.pose().scale(scale, scale, 1.0F);
        if (this.current.look == Callout.Look.FIRE) {
            fire(context, font, text, textWidth, age, alpha);
        } else {
            rupture(context, font, text, textWidth, age, alpha);
        }
        context.pose().popPose();
        RenderSystem.disableBlend();
    }

    private static void fire(GuiGraphics context, Font font, String text, int textWidth, float age, float alpha) {
        rules(context, textWidth, alpha, 0xFF9A1F, 0xE8421A);
        // embers
        for (int i = 0; i < 14; i++) {
            float life = Mth.frac(age / 22.0F + hash(i, 1));
            float x = (hash(i, 2) - 0.5F) * (textWidth + 10) + Mth.sin(age * 0.2F + i) * 1.5F;
            float y = 3.0F - life * 15.0F;
            int ember = color(mix(0xFFE27A, 0xE8421A, life), alpha * (1.0F - life) * 0.9F);
            if (ember != 0) {
                context.fill((int) x, (int) y, (int) x + 1, (int) y + 1, ember);
            }
        }
        float x = -textWidth / 2.0F;
        for (int i = 0; i < text.length(); i++) {
            Component glyph = Component.literal(String.valueOf(text.charAt(i))).setStyle(STYLE);
            // heat shimmer
            float y = -4.0F + Mth.sin(age * 0.35F + i * 0.9F) * 0.9F;
            float flame = 0.5F + 0.5F * Mth.sin(age * 0.3F - i * 0.7F);
            int body = flame < 0.5F ? mix(0xE8421A, 0xFF9A1F, flame * 2.0F) : mix(0xFF9A1F, 0xFFF27A, flame * 2.0F - 1.0F);
            glow(context, font, glyph, x, y, color(0xFF5A14, alpha * 0.28F));
            draw(context, font, glyph, x, y + 1.0F, color(0x5A1200, alpha * 0.9F));
            draw(context, font, glyph, x, y, color(body, alpha));
            x += font.width(glyph) + 1;
        }
    }

    private static void rupture(GuiGraphics context, Font font, String text, int textWidth, float age, float alpha) {
        rules(context, textWidth, alpha, 0xD3122A, 0x3A0008);
        // wind streaks
        for (int i = 0; i < 6; i++) {
            float life = Mth.frac(age / 9.0F + hash(i, 3));
            int length = 6 + (int) (hash(i, 4) * 12);
            int x = (int) (-textWidth / 2.0F - 14 + life * (textWidth + 28));
            int y = -6 + (int) (hash(i, 5) * 12);
            int streak = color(0xFF4A3D, alpha * Mth.sin(life * Mth.PI) * 0.55F);
            if (streak != 0) {
                context.fill(x, y, x + length, y + 1, streak);
            }
        }
        float shake = Math.max(0.35F, 2.2F - age * 0.25F);
        float x = -textWidth / 2.0F + Mth.sin(age * 2.9F) * shake;
        for (int i = 0; i < text.length(); i++) {
            Component glyph = Component.literal(String.valueOf(text.charAt(i))).setStyle(STYLE);
            float y = -4.0F + Mth.sin(age * 3.7F + i * 2.1F) * shake * 0.35F;
            float pulse = 0.5F + 0.5F * Mth.sin(age * 0.45F + i * 0.5F);
            glow(context, font, glyph, x, y, color(0xB00018, alpha * 0.3F));
            // split copy, dark one way and red the other
            draw(context, font, glyph, x + 1.5F, y, color(0x14000A, alpha * 0.85F));
            draw(context, font, glyph, x - 1.0F, y, color(0x7A0010, alpha * 0.7F));
            draw(context, font, glyph, x, y, color(mix(0xD3122A, 0xFF5A48, pulse), alpha));
            x += font.width(glyph) + 1;
        }
    }

    private static void rules(GuiGraphics context, int textWidth, float alpha, int near, int far) {
        int half = textWidth / 2 + 5;
        int length = 22;
        for (int i = 0; i < length; i++) {
            float fade = 1.0F - i / (float) length;
            int line = color(mix(far, near, fade), alpha * fade * 0.85F);
            if (line != 0) {
                context.fill(half + i, 0, half + i + 1, 1, line);
                context.fill(-half - i - 1, 0, -half - i, 1, line);
            }
        }
    }

    private static void glow(GuiGraphics context, Font font, Component glyph, float x, float y, int color) {
        draw(context, font, glyph, x - 1.0F, y, color);
        draw(context, font, glyph, x + 1.0F, y, color);
        draw(context, font, glyph, x, y - 1.0F, color);
        draw(context, font, glyph, x, y + 1.0F, color);
    }

    private static void draw(GuiGraphics context, Font font, Component glyph, float x, float y, int color) {
        if (color == 0) {
            return;
        }
        context.pose().pushPose();
        context.pose().translate(x, y, 0.0F);
        context.drawString(font, glyph, 0, 0, color, false);
        context.pose().popPose();
    }

    // near zero alpha text draws fully solid, skip it
    private static int color(int rgb, float alpha) {
        int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        return a < 8 ? 0 : a << 24 | rgb;
    }

    private static int mix(int from, int to, float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        int r = (int) Mth.lerpInt(t, from >> 16 & 255, to >> 16 & 255);
        int g = (int) Mth.lerpInt(t, from >> 8 & 255, to >> 8 & 255);
        int b = (int) Mth.lerpInt(t, from & 255, to & 255);
        return r << 16 | g << 8 | b;
    }

    private static float hash(int index, int salt) {
        return Mth.frac(Mth.sin(index * 12.9898F + salt * 78.233F) * 43758.547F);
    }
}
