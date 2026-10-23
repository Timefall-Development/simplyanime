/*
package net.hussain.simplyanime.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.platform.ClientPlatform;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class AddonRenderTypes extends RenderType {

    public static final ResourceLocation WHITE = Constants.location("minecraft", "textures/misc/white.png");

    // additive like lightning, no depth write
    private static final RenderType GLOW = RenderType.create("simplyanime_glow",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 262144, false, true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private static final RenderType SHADE = RenderType.create("simplyanime_shade",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 262144, false, true,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    // textured versions of the two above
    private static final Function<ResourceLocation, RenderType> ADDITIVE = Util.memoize(id -> RenderType.create("simplyanime_additive",
            ClientPlatform.TEXTURED_FORMAT, VertexFormat.Mode.QUADS, 262144, false, true,
            CompositeState.builder()
                    .setShaderState(ClientPlatform.TEXTURED_SHADER)
                    .setTextureState(new TextureStateShard(id, false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> BLENDED = Util.memoize(id -> RenderType.create("simplyanime_blended",
            ClientPlatform.TEXTURED_FORMAT, VertexFormat.Mode.QUADS, 262144, false, true,
            CompositeState.builder()
                    .setShaderState(ClientPlatform.TEXTURED_SHADER)
                    .setTextureState(new TextureStateShard(id, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> PACK_ENTITY = Util.memoize(id -> RenderType.create("simplyanime_entity_pack",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 262144, true, true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new TextureStateShard(id, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(OVERLAY)
                    .setLightmapState(LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> SOFT_ADDITIVE = Util.memoize(id -> RenderType.create("simplyanime_soft_additive",
            ClientPlatform.TEXTURED_FORMAT, VertexFormat.Mode.QUADS, 262144, false, true,
            CompositeState.builder()
                    .setShaderState(ClientPlatform.TEXTURED_SHADER)
                    .setTextureState(new TextureStateShard(id, true, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> SOFT_BLENDED = Util.memoize(id -> RenderType.create("simplyanime_soft_blended",
            ClientPlatform.TEXTURED_FORMAT, VertexFormat.Mode.QUADS, 262144, false, true,
            CompositeState.builder()
                    .setShaderState(ClientPlatform.TEXTURED_SHADER)
                    .setTextureState(new TextureStateShard(id, true, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)));

    private record PackKey(ResourceLocation texture, boolean smooth, int kind) {
    }

    private static final int ADD = 0;
    private static final int BLEND = 1;
    private static final int FORWARD_ADD = 2;
    private static final int FORWARD_BLEND = 3;
    private static final int SOLID = 4;
    private static final Set<RenderType> SOLIDS = new HashSet<>();

    private static final Function<PackKey, RenderType> PACK = Util.memoize(key -> {
        boolean forward = key.kind == FORWARD_ADD || key.kind == FORWARD_BLEND;
        boolean additive = key.kind == ADD || key.kind == FORWARD_ADD;
        CompositeState.CompositeStateBuilder builder = CompositeState.builder()
                .setShaderState(forward ? RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER : RENDERTYPE_BEACON_BEAM_SHADER)
                .setTextureState(new TextureStateShard(key.texture, key.smooth, false))
                .setTransparencyState(key.kind == SOLID ? NO_TRANSPARENCY : additive ? LIGHTNING_TRANSPARENCY : TRANSLUCENT_TRANSPARENCY)
                .setWriteMaskState(key.kind == SOLID ? COLOR_DEPTH_WRITE : COLOR_WRITE)
                .setCullState(NO_CULL);
        if (forward) {
            builder.setOverlayState(OVERLAY).setLightmapState(LIGHTMAP);
        }
        return RenderType.create("simplyanime_pack_" + key.kind + (key.smooth ? "_soft" : ""),
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 262144, false,
                key.kind != SOLID, builder.createCompositeState(false));
    });

    private static RenderType pack(ResourceLocation id, boolean smooth, boolean additive) {
        int kind = additive ? ADD : BLEND;
        if (ShaderCompat.packNameHas("bliss")) {
            // bliss drops see through beacons
            kind = additive ? FORWARD_ADD : FORWARD_BLEND;
        } else if (ShaderCompat.packNameHas("photon")) {
            // photon ignores additive
            kind = BLEND;
        }
        return PACK.apply(new PackKey(id, smooth, kind));
    }

    private static final Function<ResourceLocation, RenderType> DEPTH = Util.memoize(id -> RenderType.create("simplyanime_depth",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 262144, false, false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                    .setTextureState(new TextureStateShard(id, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(DEPTH_WRITE)
                    .createCompositeState(false)));

    private AddonRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size,
                              boolean crumbling, boolean translucent, Runnable start, Runnable end) {
        super(name, format, mode, size, crumbling, translucent, start, end);
    }

    // untextured glow
    public static RenderType glow() {
        return ShaderCompat.packInUse() ? pack(WHITE, false, true) : GLOW;
    }

    // untextured shading
    public static RenderType shade() {
        return ShaderCompat.packInUse() ? pack(WHITE, false, false) : SHADE;
    }

    public static RenderType additive(ResourceLocation texture) {
        return ShaderCompat.packInUse() ? pack(texture, false, true) : ADDITIVE.apply(texture);
    }

    public static RenderType blended(ResourceLocation texture) {
        return ShaderCompat.packInUse() ? pack(texture, false, false) : BLENDED.apply(texture);
    }

    public static RenderType softAdditive(ResourceLocation texture) {
        return ShaderCompat.packInUse() ? pack(texture, true, true) : SOFT_ADDITIVE.apply(texture);
    }

    public static RenderType softBlended(ResourceLocation texture) {
        return ShaderCompat.packInUse() ? pack(texture, true, false) : SOFT_BLENDED.apply(texture);
    }

    public static RenderType solid(ResourceLocation texture) {
        if (!ShaderCompat.packInUse()) {
            return SOFT_BLENDED.apply(texture);
        }
        RenderType layer = PACK.apply(new PackKey(texture, true, SOLID));
        SOLIDS.add(layer);
        return layer;
    }

    public static boolean isSolid(RenderType layer) {
        return SOLIDS.contains(layer);
    }

    public static RenderType depth(ResourceLocation texture) {
        return DEPTH.apply(texture);
    }

    // entity translucent, or the emissive one under a pack
    public static RenderType entityTranslucent(ResourceLocation texture) {
        return ShaderCompat.packInUse() ? PACK_ENTITY.apply(texture) : RenderType.entityTranslucent(texture);
    }
}
*/