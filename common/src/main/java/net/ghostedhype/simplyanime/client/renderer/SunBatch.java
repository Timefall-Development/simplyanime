package net.ghostedhype.simplyanime.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SunBatch {

    private static final int[] WHITE = {255, 255, 255};
    // how much of a flame's colour gets added again on top
    private static final float LIT = 0.35F;
    private static final int LON = 40;
    private static final int LAT = 20;
    private static final float[][][] BALL = new float[LAT + 1][LON + 1][];

    static {
        for (int j = 0; j <= LAT; j++) {
            float lat = (j / (float) LAT - 0.5F) * Mth.PI;
            float ring = Mth.cos(lat);
            for (int i = 0; i <= LON; i++) {
                float lon = i * Mth.TWO_PI / LON;
                BALL[j][i] = new float[]{ring * Mth.cos(lon), Mth.sin(lat), ring * Mth.sin(lon), ring};
            }
        }
    }

    private final Map<ResourceLocation, List<float[]>> additive = new LinkedHashMap<>();
    private final Map<ResourceLocation, List<float[]>> blended = new LinkedHashMap<>();
    private final List<float[]> depth = new ArrayList<>();
    private final Map<ResourceLocation, List<float[]>> solid = new LinkedHashMap<>();
    private final float soft = ShaderCompat.packInUse() ? 0.12F : 1.0F;
    final Vec3 right;
    final Vec3 up;
    // camera relative to the entity
    final Vec3 eye;

    public SunBatch(Vec3 right, Vec3 up, Vec3 eye) {
        this.right = right;
        this.up = up;
        this.eye = eye;
    }

    float facing(Vec3 c, Vec3 n, float radius) {
        Vec3 p = c.add(n.scale(radius));
        return (float) n.dot(this.eye.subtract(p).normalize());
    }

    void ball(boolean solid, ResourceLocation texture, Vec3 c, float radius, float tilt, float slide,
              int[] core, int[] rim, float coreAlpha, float rimAlpha) {
        if (radius <= 0.001F || (coreAlpha <= 0.005F && rimAlpha <= 0.005F)) {
            return;
        }
        // opaque under packs when nothing shows through
        boolean opaque = solid && coreAlpha > 0.95F && rimAlpha > 0.95F && ShaderCompat.packInUse();
        List<float[]> list = (opaque ? this.solid : solid ? this.blended : this.additive).computeIfAbsent(texture, k -> new ArrayList<>());
        float ct = Mth.cos(tilt);
        float st = Mth.sin(tilt);
        // camera inside the ball
        boolean inside = this.eye.distanceToSqr(c) < radius * radius;
        float[][] corners = new float[4][];
        for (int j = 0; j < LAT; j++) {
            for (int i = 0; i < LON; i++) {
                boolean seen = false;
                for (int k = 0; k < 4; k++) {
                    int ii = i + (k == 1 || k == 2 ? 1 : 0);
                    int jj = j + (k >= 2 ? 1 : 0);
                    float[] unit = BALL[jj][ii];
                    Vec3 n = new Vec3(unit[0], unit[1] * ct - unit[2] * st, unit[1] * st + unit[2] * ct);
                    Vec3 p = c.add(n.scale(radius));
                    float facing = (float) n.dot(this.eye.subtract(p).normalize());
                    if (inside) {
                        facing = -facing;
                    }
                    seen |= facing > 0.0F;
                    float f = Mth.sqrt(Mth.clamp(facing, 0.0F, 1.0F));
                    // uv pinches at the poles, fade it
                    float alpha = Mth.lerp(f, rimAlpha, coreAlpha) * (solid ? 1.0F : Math.min(1.0F, unit[3] * 2.5F));
                    corners[k] = corner(p, mix(rim, core, f), alpha, ii * 2.0F / LON + slide, jj / (float) LAT);
                }
                if (seen) {
                    for (float[] corner : corners) {
                        list.add(corner);
                    }
                }
            }
        }
    }

    // packs only, see AddonRenderTypes.depth
    void depthBall(Vec3 c, float radius) {
        if (!ShaderCompat.packInUse() || radius <= 0.001F || this.eye.distanceToSqr(c) < radius * radius) {
            return;
        }
        float[][] corners = new float[4][];
        for (int j = 0; j < LAT; j++) {
            for (int i = 0; i < LON; i++) {
                boolean seen = false;
                for (int k = 0; k < 4; k++) {
                    float[] unit = BALL[j + (k >= 2 ? 1 : 0)][i + (k == 1 || k == 2 ? 1 : 0)];
                    Vec3 n = new Vec3(unit[0], unit[1], unit[2]);
                    Vec3 p = c.add(n.scale(radius));
                    seen |= n.dot(this.eye.subtract(p)) > 0.0;
                    corners[k] = corner(p, WHITE, 1.0F, 0.5F, 0.5F);
                }
                if (seen) {
                    for (float[] corner : corners) {
                        this.depth.add(corner);
                    }
                }
            }
        }
    }

    // flame on a ball at n, facing the camera
    void tongue(Vec3 c, Vec3 n, float from, float length, float halfWidth, int variant, int[] color, float alpha) {
        Vec3 foot = c.add(n.scale(from));
        Vec3 side = n.cross(this.eye.subtract(foot));
        if (side.lengthSqr() < 1.0E-6 || length <= 0.001F) {
            return;
        }
        float u0 = (variant & 3) * 0.25F;
        this.fire(SunRenderTypes.FLAMES, foot.add(n.scale(length * 0.5F)), side.normalize(), n,
                halfWidth, length * 0.5F, color, alpha, u0, 0, u0 + 0.25F, 1);
    }

    // camera facing strip along a path
    void ribbon(ResourceLocation texture, Vec3[] path, float halfFrom, float halfTo, int[] color, float alphaFrom, float alphaTo, float scroll) {
        if (alphaFrom <= 0.005F && alphaTo <= 0.005F) {
            return;
        }
        List<float[]> list = this.blended.computeIfAbsent(texture, k -> new ArrayList<>());
        List<float[]> lit = this.additive.computeIfAbsent(texture, k -> new ArrayList<>());
        int last = path.length - 1;
        Vec3[] sides = new Vec3[path.length];
        for (int i = 0; i <= last; i++) {
            Vec3 along = path[Math.min(last, i + 1)].subtract(path[Math.max(0, i - 1)]);
            Vec3 side = along.cross(this.eye.subtract(path[i]));
            sides[i] = side.lengthSqr() < 1.0E-6 ? this.right : side.normalize();
        }
        for (int i = 0; i < last; i++) {
            float t0 = i / (float) last;
            float t1 = (i + 1) / (float) last;
            Vec3 w0 = sides[i].scale(Mth.lerp(t0, halfFrom, halfTo));
            Vec3 w1 = sides[i + 1].scale(Mth.lerp(t1, halfFrom, halfTo));
            float a0 = Mth.lerp(t0, alphaFrom, alphaTo);
            float a1 = Mth.lerp(t1, alphaFrom, alphaTo);
            for (int pass = 0; pass < 2; pass++) {
                List<float[]> into = pass == 0 ? list : lit;
                float k = pass == 0 ? 1.0F : LIT;
                into.add(corner(path[i].subtract(w0), color, a0 * k, 0.0F, 1.0F - t0 + scroll));
                into.add(corner(path[i].add(w0), color, a0 * k, 1.0F, 1.0F - t0 + scroll));
                into.add(corner(path[i + 1].add(w1), color, a1 * k, 1.0F, 1.0F - t1 + scroll));
                into.add(corner(path[i + 1].subtract(w1), color, a1 * k, 0.0F, 1.0F - t1 + scroll));
            }
        }
    }

    // additive
    void glow(Vec3 c, float radius, int[] color, float alpha) {
        alpha *= this.soft;
        this.quad(this.additive, SunRenderTypes.GLOW, c, this.right, this.up, radius, radius, color, alpha, 0, 0, 1, 1);
    }

    // blended
    void halo(Vec3 c, float radius, int[] color, float alpha) {
        alpha *= this.soft;
        this.fire(SunRenderTypes.GLOW, c, this.right, this.up, radius, radius, color, alpha, 0, 0, 1, 1);
    }

    void flatGlow(Vec3 c, float radius, int[] color, float alpha) {
        alpha *= this.soft;
        this.fire(SunRenderTypes.GLOW, c, new Vec3(1, 0, 0), new Vec3(0, 0, 1), radius, radius,
                color, alpha, 0, 0, 1, 1);
    }

    void flatRing(Vec3 c, float radius, int[] color, float alpha) {
        this.fire(SunRenderTypes.RING, c, new Vec3(1, 0, 0), new Vec3(0, 0, 1), radius, radius,
                color, alpha, 0, 0, 1, 1);
    }

    void facingRing(Vec3 c, float radius, int[] color, float alpha) {
        this.fire(SunRenderTypes.RING, c, this.right, this.up, radius, radius, color, alpha, 0, 0, 1, 1);
    }

    // blended, then a bit additive
    private void fire(ResourceLocation texture, Vec3 c, Vec3 u, Vec3 v, float hu, float hv, int[] color, float alpha,
                      float u0, float v0, float u1, float v1) {
        this.quad(this.blended, texture, c, u, v, hu, hv, color, alpha, u0, v0, u1, v1);
        this.quad(this.additive, texture, c, u, v, hu, hv, color, alpha * LIT, u0, v0, u1, v1);
    }

    void smoke(Vec3 c, float radius, float spin, int[] color, float alpha) {
        Vec3 u = this.right.scale(Mth.cos(spin)).add(this.up.scale(Mth.sin(spin)));
        Vec3 v = this.right.scale(-Mth.sin(spin)).add(this.up.scale(Mth.cos(spin)));
        this.quad(this.blended, SunRenderTypes.SMOKE, c, u, v, radius, radius, color, alpha, 0, 0, 1, 1);
    }

    void ray(Vec3 c, float a, float from, float to, float halfWidth, int[] color, float alpha) {
        Vec3 dir = this.right.scale(Mth.cos(a)).add(this.up.scale(Mth.sin(a)));
        Vec3 side = this.right.scale(-Mth.sin(a)).add(this.up.scale(Mth.cos(a)));
        Vec3 mid = c.add(dir.scale((from + to) * 0.5F));
        this.quad(this.additive, SunRenderTypes.RAY, mid, side, dir, halfWidth, (to - from) * 0.5F, color, alpha, 0, 0, 1, 1);
    }

    void streak(ResourceLocation texture, Vec3 a, Vec3 b, Vec3 w, float halfA, float halfB, int[] color, float alpha, float scroll) {
        List<float[]> list = this.blended.computeIfAbsent(texture, k -> new ArrayList<>());
        float u0 = 0.0F;
        float u1 = texture == SunRenderTypes.FLAMES ? 0.25F : 1.0F;
        list.add(corner(a.add(w.scale(-halfA)), color, alpha, u0, 1 + scroll));
        list.add(corner(a.add(w.scale(halfA)), color, alpha, u1, 1 + scroll));
        list.add(corner(b.add(w.scale(halfB)), color, alpha * 0.2F, u1, scroll));
        list.add(corner(b.add(w.scale(-halfB)), color, alpha * 0.2F, u0, scroll));
    }

    private void quad(Map<ResourceLocation, List<float[]>> target, ResourceLocation texture, Vec3 c, Vec3 u, Vec3 v,
                      float hu, float hv, int[] color, float alpha, float u0, float v0, float u1, float v1) {
        if (alpha <= 0.005F || hu <= 0.001F || hv <= 0.001F) {
            return;
        }
        List<float[]> list = target.computeIfAbsent(texture, k -> new ArrayList<>());
        Vec3 du = u.scale(hu);
        Vec3 dv = v.scale(hv);
        // v0 is the top edge (along +v), v1 the bottom edge
        list.add(corner(c.subtract(du).subtract(dv), color, alpha, u0, v1));
        list.add(corner(c.add(du).subtract(dv), color, alpha, u1, v1));
        list.add(corner(c.add(du).add(dv), color, alpha, u1, v0));
        list.add(corner(c.subtract(du).add(dv), color, alpha, u0, v0));
    }

    private static float[] corner(Vec3 p, int[] color, float alpha, float u, float v) {
        return new float[]{(float) p.x, (float) p.y, (float) p.z, color[0], color[1], color[2],
                Mth.clamp(alpha, 0.0F, 1.0F) * 255, u, v};
    }

    void flush(Matrix4f m, MultiBufferSource buffers) {
        this.solid.forEach((texture, list) -> emit(m, VfxBuffer.of(buffers, AddonRenderTypes.solid(texture)), list));
        if (!this.depth.isEmpty()) {
            emit(m, VfxBuffer.of(buffers, AddonRenderTypes.depth(AddonRenderTypes.WHITE)), this.depth);
        }
        // smoke first so the fire glows over it
        this.blended.forEach((texture, list) -> emit(m, VfxBuffer.of(buffers, SunRenderTypes.blended(texture)), list));
        this.additive.forEach((texture, list) -> emit(m, VfxBuffer.of(buffers, SunRenderTypes.additive(texture)), list));
    }

    private static void emit(Matrix4f m, VfxBuffer vc, List<float[]> list) {
        for (float[] q : list) {
            vc.vertex(m, q[0], q[1], q[2], (int) q[3], (int) q[4], (int) q[5], (int) q[6], q[7], q[8]);
        }
    }

    static int[] mix(int[] from, int[] to, float t) {
        return new int[]{(int) Mth.lerpInt(t, from[0], to[0]), (int) Mth.lerpInt(t, from[1], to[1]),
                (int) Mth.lerpInt(t, from[2], to[2])};
    }
}
