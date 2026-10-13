package net.ghostedhype.simplyanime.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public abstract class ClientPlatform extends RenderStateShard {

    // position, colour and texture without lighting
    public static final VertexFormat TEXTURED_FORMAT = DefaultVertexFormat.POSITION_COLOR_TEX;
    public static final ShaderStateShard TEXTURED_SHADER = POSITION_COLOR_TEX_SHADER;

    // particles that add their light to what is behind them
    public static final ParticleRenderType ADDITIVE_PARTICLES = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textures) {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return "simplyanime:additive";
        }
    };

    private ClientPlatform() {
        super("simplyanime_platform", () -> {
        }, () -> {
        });
    }

    public static ResourceLocation skin(AbstractClientPlayer player) {
        return player.getSkinTextureLocation();
    }

    public static float partialTick() {
        return Minecraft.getInstance().getFrameTime();
    }

    public static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int a) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }

    public static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int a,
                              float u, float v) {
        consumer.vertex(matrix, x, y, z).color(r, g, b, a).uv(u, v).endVertex();
    }

    public static void entityVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z,
                                    int r, int g, int b, int a, float u, float v, int light, float nx, float ny, float nz) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    public static void particleVertex(VertexConsumer consumer, double x, double y, double z, float u, float v,
                                      float r, float g, float b, float a, int light) {
        consumer.vertex(x, y, z).uv(u, v).color(r, g, b, a).uv2(light).endVertex();
    }
}
