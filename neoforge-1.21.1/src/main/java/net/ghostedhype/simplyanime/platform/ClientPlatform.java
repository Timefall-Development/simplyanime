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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class ClientPlatform extends RenderStateShard {

    // position, colour and texture without lighting
    public static final VertexFormat TEXTURED_FORMAT = DefaultVertexFormat.POSITION_TEX_COLOR;
    public static final ShaderStateShard TEXTURED_SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);

    // particles that add their light to what is behind them
    public static final ParticleRenderType ADDITIVE_PARTICLES = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textures) {
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
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
        return player.getSkin().texture();
    }

    public static float partialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
    }

    public static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    public static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int a,
                              float u, float v) {
        consumer.addVertex(matrix, x, y, z).setUv(u, v).setColor(r, g, b, a);
    }

    public static void entityVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z,
                                    int r, int g, int b, int a, float u, float v, int light, float nx, float ny, float nz) {
        Vector3f transformedNormal = normal.transform(new Vector3f(nx, ny, nz));
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(transformedNormal.x(), transformedNormal.y(), transformedNormal.z());
    }

    public static void particleVertex(VertexConsumer consumer, double x, double y, double z, float u, float v,
                                      float r, float g, float b, float a, int light) {
        consumer.addVertex((float) x, (float) y, (float) z).setUv(u, v).setColor(r, g, b, a).setLight(light);
    }
}
