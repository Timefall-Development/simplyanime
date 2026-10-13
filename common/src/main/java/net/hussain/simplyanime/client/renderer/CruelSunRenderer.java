package net.hussain.simplyanime.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.hussain.simplyanime.config.RhittaConfig;
import net.hussain.simplyanime.entity.CruelSunEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

// sun and fireball are sphere meshes shaded by how much each bit faces the camera, flames on top.
// fire stays inside what CruelSunEntity damages (HEAT_EDGE, TRAIL_*, blast front), only light and smoke go past.
// fire is drawn blended first then a bit of additive on top, additive alone goes white against a day sky
public class CruelSunRenderer extends EntityRenderer<CruelSunEntity> {

    private static final int[] WHITE = {255, 250, 235};
    private static final int[] PALE = {255, 238, 170};
    private static final int[] GOLD = {255, 196, 70};
    private static final int[] ORANGE = {255, 120, 20};
    private static final int[] RED = {220, 50, 10};
    private static final int[] SMOKE = {58, 46, 40};
    private static final int[] BODY = {255, 222, 120};
    private static final int[] LIMB = {238, 104, 18};
    private static final int[] FLAME = {255, 96, 8};

    // axis tilt, spin in radians per tick
    private static final float TILT = 0.42F;
    private static final float SPIN = 0.012F;

    public CruelSunRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CruelSunEntity entity) {
        return SunRenderTypes.GLOW;
    }

    @Override
    public boolean shouldRender(CruelSunEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(CruelSunEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light) {
        // shadow pass: only the lifted blocks cast shadows
        if (ShaderCompat.shadowPass()) {
            debrisOnly(entity, poseStack, buffers, partialTick);
            return;
        }
        Quaternionf rotation = this.entityRenderDispatcher.cameraOrientation();
        Vec3 right = VfxBuffer.rotate(rotation, 1, 0, 0);
        Vec3 up = VfxBuffer.rotate(rotation, 0, 1, 0);
        Vec3 base = entity.getPosition(partialTick);
        Vec3 camera = this.entityRenderDispatcher.camera.getPosition();
        float time = entity.tickCount + partialTick;
        float sunLevel = entity.getSun();
        SunBatch batch = new SunBatch(right, up, camera.subtract(base));

        switch (entity.getPhase()) {
            case CruelSunEntity.PHASE_CHARGE -> {
                LivingEntity owner = entity.getOwner();
                if (owner == null) {
                    return;
                }
                float ticks = entity.getPhaseTicks(partialTick);
                float orb = entity.getOrbRadius(partialTick);
                Vec3 center = CruelSunEntity.chargeCenter(owner, partialTick, orb).subtract(base);
                Vec3 feet = owner.getPosition(partialTick).subtract(base);
                boolean formed = ticks >= CruelSunEntity.IGNITE_TICKS;
                birth(batch, feet, center, entity.getOrbBase(), time, ticks);
                if (formed) {
                    debris(entity, owner, base, poseStack, buffers, partialTick, time, ticks - CruelSunEntity.IGNITE_TICKS, 0.0F);
                    scorched(entity, owner, base, poseStack, buffers, partialTick, ticks - CruelSunEntity.IGNITE_TICKS, 1.0F);
                    ground(batch, feet, orb, ticks - CruelSunEntity.IGNITE_TICKS, sunLevel);
                    // eruption flash
                    float since = ticks - CruelSunEntity.IGNITE_TICKS;
                    ignition(batch, feet, center, entity.getOrbBase(), since);
                    sun(batch, center, orb, time, sunLevel, detail(camera, base.add(center)), 1.0F);
                } else {
                    // small flickering light until it erupts
                    float gather = ticks / CruelSunEntity.IGNITE_TICKS;
                    float flick = 0.85F + 0.15F * Mth.sin(time * 1.3F);
                    float spark = entity.getOrbBase() * (0.35F + 0.75F * gather);
                    batch.halo(center, spark * 2.8F * flick, FLAME, 0.7F);
                    batch.halo(center, spark * 1.7F, GOLD, 0.9F);
                    batch.halo(center, spark * 0.9F, WHITE, 1.0F);
                    rays(batch, center, spark * 0.4F, spark * (2.0F + 2.0F * gather), spark * 0.12F, 10, time * 3.0F, PALE, 0.7F);
                }
            }
            case CruelSunEntity.PHASE_FLIGHT -> {
                // rubble drops after the throw
                LivingEntity owner = entity.getOwner();
                float flight = entity.getPhaseTicks(partialTick);
                if (owner != null && flight < DEBRIS_FALL) {
                    debris(entity, owner, base, poseStack, buffers, partialTick, time, 1000.0F, flight / DEBRIS_FALL);
                }
                if (owner != null && flight < FIRE_FADE) {
                    scorched(entity, owner, base, poseStack, buffers, partialTick, 1000.0F, 1.0F - flight / FIRE_FADE);
                }
                float orb = entity.getOrbRadius(partialTick);
                Vec3 dir = entity.getAim();
                trail(batch, dir, orb, time, base.subtract(camera));
                sun(batch, Vec3.ZERO, orb, time, sunLevel, detail(camera, base), 1.0F);
            }
            case CruelSunEntity.PHASE_BLAST -> blast(batch, entity, partialTick, time, sunLevel, detail(camera, base));
            default -> {
            }
        }
        batch.flush(poseStack.last().pose(), buffers);
    }

    private static void debrisOnly(CruelSunEntity entity, PoseStack poseStack, MultiBufferSource buffers, float partialTick) {
        LivingEntity owner = entity.getOwner();
        if (owner == null) {
            return;
        }
        Vec3 base = entity.getPosition(partialTick);
        float time = entity.tickCount + partialTick;
        float ticks = entity.getPhaseTicks(partialTick);
        if (entity.getPhase() == CruelSunEntity.PHASE_CHARGE && ticks >= CruelSunEntity.IGNITE_TICKS) {
            debris(entity, owner, base, poseStack, buffers, partialTick, time, ticks - CruelSunEntity.IGNITE_TICKS, 0.0F);
        } else if (entity.getPhase() == CruelSunEntity.PHASE_FLIGHT && ticks < DEBRIS_FALL) {
            debris(entity, owner, base, poseStack, buffers, partialTick, time, 1000.0F, ticks / DEBRIS_FALL);
        }
    }

    private static final int DEBRIS = 26;
    private static final float DEBRIS_FALL = 14.0F;

    // lifted ground chunks, block models from whatever is under each spot. fall 0..1 after the throw
    private static void debris(CruelSunEntity sun, LivingEntity owner, Vec3 base, PoseStack poseStack,
                               MultiBufferSource buffers, float partialTick, float time, float since, float fall) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        BlockRenderDispatcher blocks = client.getBlockRenderer();
        Vec3 feet = owner.getPosition(partialTick);
        float spread = Math.max(3.0F, sun.getOrbBase() * 1.4F);
        for (int i = 0; i < DEBRIS; i++) {
            float f1 = ((i * 37) % 23) / 22.0F;
            float f2 = ((i * 53) % 19) / 18.0F;
            float f3 = ((i * 71) % 17) / 16.0F;
            float angle = i * 2.399F + time * 0.004F;
            float dist = 1.8F + f1 * spread;
            double x = feet.x + Mth.cos(angle) * dist;
            double z = feet.z + Mth.sin(angle) * dist;
            BlockPos.MutableBlockPos pos = BlockPos.containing(x, feet.y + 2, z).mutable();
            BlockState state = client.level.getBlockState(pos);
            for (int d = 0; d < 7 && (state.isAir() || !state.canOcclude()); d++) {
                pos.move(0, -1, 0);
                state = client.level.getBlockState(pos);
            }
            if (state.isAir() || !state.canOcclude()) {
                continue;
            }
            // staggered
            float rise = Mth.clamp((since - i * 0.6F) / 18.0F, 0.0F, 1.0F);
            rise = rise * rise * (3.0F - 2.0F * rise);
            float height = rise * (1.2F + f2 * 3.8F) + rise * 0.35F * Mth.sin(time * 0.09F + i * 1.3F);
            height *= 1.0F - fall * fall;
            if (height < 0.05F && fall > 0) {
                continue;
            }
            float size = (0.3F + f3 * 0.45F) * Math.max(0.35F, rise);
            poseStack.pushPose();
            poseStack.translate(x - base.x, pos.getY() + 1.0 + height - base.y, z - base.z);
            poseStack.mulPose(Axis.YP.rotation(time * 0.02F * (0.5F + f1) + i));
            poseStack.mulPose(Axis.XP.rotation(time * 0.015F * (0.5F + f2) + i * 0.7F));
            poseStack.scale(size, size, size);
            poseStack.translate(-0.5, -0.5, -0.5);
            blocks.renderSingleBlock(state, poseStack, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    private static final int FIRE_SPOTS = 40;
    private static final float FIRE_FADE = 40.0F;

    // fake fire on plants and leaves, only the fire model drawn there. keep 1..0 after the throw
    private static void scorched(CruelSunEntity sun, LivingEntity owner, Vec3 base, PoseStack poseStack,
                                 MultiBufferSource buffers, float partialTick, float since, float keep) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || !RhittaConfig.get().cruelSun.scorchSurroundings) {
            return;
        }
        BlockRenderDispatcher blocks = client.getBlockRenderer();
        BlockState fire = Blocks.FIRE.defaultBlockState();
        Vec3 feet = owner.getPosition(partialTick);
        float reach = Math.max(3.0F, sun.getOrbBase() * 1.8F);
        int shown = (int) (FIRE_SPOTS * keep);
        for (int i = 0; i < shown; i++) {
            if (since < i * 0.8F) {
                break;
            }
            float f1 = ((i * 41) % 29) / 28.0F;
            float angle = i * 2.399F + 0.7F;
            float dist = 1.5F + f1 * reach;
            BlockPos.MutableBlockPos pos = BlockPos.containing(feet.x + Mth.cos(angle) * dist, feet.y + 3,
                    feet.z + Mth.sin(angle) * dist).mutable();
            BlockState state = client.level.getBlockState(pos);
            for (int d = 0; d < 7 && state.isAir(); d++) {
                pos.move(0, -1, 0);
                state = client.level.getBlockState(pos);
            }
            if (state.isAir() || !state.isFlammable(client.level, pos, Direction.UP)) {
                continue;
            }
            // plants burn in place, leaves and wood on top
            if (!state.canBeReplaced()) {
                pos.move(0, 1, 0);
                if (!client.level.getBlockState(pos).isAir()) {
                    continue;
                }
            }
            poseStack.pushPose();
            poseStack.translate(pos.getX() - base.x, pos.getY() - base.y, pos.getZ() - base.z);
            blocks.renderSingleBlock(fire, poseStack, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    // fewer flames when it is far away
    private static float detail(Vec3 camera, Vec3 at) {
        double d = camera.distanceTo(at);
        return d < 64 ? 1.0F : d < 160 ? 0.6F : 0.35F;
    }

    private static void sun(SunBatch b, Vec3 c, float r, float time, float sunLevel, float detail, float alpha) {
        float pulse = 1.0F + 0.035F * Mth.sin(time * 0.5F) + 0.02F * Mth.sin(time * 1.7F);
        float bright = 0.8F + 0.2F * sunLevel;
        float body = r * pulse;
        float turned = time * SPIN;
        // how far the surface texture has slid round, one full turn is 2
        float slide = -turned / Mth.PI;
        // bloom first, kept low so the ball still looks like a ball
        b.halo(c, r * 2.5F * pulse, FLAME, 0.5F * alpha);
        b.glow(c, r * 4.2F * pulse, GOLD, 0.15F * alpha * bright);
        rays(b, c, r * 0.9F, r * 3.6F, r * 0.12F, 10, time, GOLD, 0.3F * alpha * bright);

        // solid body so the sky doesn't show through, two fire layers sliding over it
        b.ball(true, SunRenderTypes.PLASMA_A, c, body, TILT, slide, BODY, LIMB, alpha, alpha);
        b.ball(false, SunRenderTypes.PLASMA_A, c, body * 1.005F, TILT, slide, ORANGE, FLAME, 0.45F * alpha, 0.6F * alpha);
        b.ball(false, SunRenderTypes.PLASMA_B, c, body * 1.02F, -TILT * 1.7F, -slide * 0.6F, GOLD, GOLD, 0.3F * alpha, 0.2F * alpha);
        // haze on the limb
        b.ball(true, AddonRenderTypes.WHITE, c, body * 1.08F, 0.0F, 0.0F, ORANGE, FLAME, 0.0F, 0.7F * alpha);
        // core
        b.glow(c, r * 0.9F, WHITE, 0.25F * alpha);

        // tongues all round, mostly short with a long one now and then. tips stop at HEAT_EDGE
        float edge = r * CruelSunEntity.HEAT_EDGE;
        int flames = Math.max(40, (int) (110 * detail));
        for (int i = 0; i < flames; i++) {
            Vec3 n = spot(i, flames, turned, TILT);
            float facing = b.facing(c, n, body);
            if (facing < -0.35F || facing > 0.6F) {
                continue;
            }
            float f1 = ((i * 37) % 23) / 22.0F;
            float f2 = ((i * 53) % 19) / 18.0F;
            float flick = 0.5F + 0.5F * Mth.sin(time * (0.25F + 0.2F * f1) + i * 2.39F);
            float reach = i % 3 == 0 ? 0.45F + 0.5F * flick * (0.6F + 0.4F * f1) : 0.2F + 0.35F * flick * (0.5F + f2);
            float from = body * 0.88F;
            float len = Math.min(body * reach, edge - from);
            float wide = body * (0.2F + 0.14F * f2);
            float show = alpha * Mth.clamp(1.0F - facing, 0.0F, 1.0F);
            b.tongue(c, n, from, len, wide, i, FLAME, 0.9F * show);
            b.tongue(c, n, from, len * 0.6F, wide * 0.6F, i + 1, GOLD, 0.8F * show);
        }

        // loops
        int loops = detail > 0.5F ? 8 : 4;
        for (int k = 0; k < loops; k++) {
            Vec3 n = spot(k * 7 + 3, 61, turned, TILT);
            if (b.facing(c, n, body) < -0.3F) {
                continue;
            }
            float f1 = ((k * 37) % 23) / 22.0F;
            float life = Mth.sin(Mth.PI * Mth.frac(time / 110.0F + k * 0.37F));
            Vec3 across = n.cross(new Vec3(0.3, 1.0, 0.2 + k)).normalize();
            float span = 0.55F + 0.25F * f1;
            float height = (0.18F + 0.3F * f1) * life;
            Vec3[] path = new Vec3[11];
            for (int j = 0; j < path.length; j++) {
                float t = j / (float) (path.length - 1);
                float ang = (t - 0.5F) * span;
                Vec3 dir = n.scale(Mth.cos(ang)).add(across.scale(Mth.sin(ang)));
                path[j] = c.add(dir.scale(body * (0.97F + height * Mth.sin(Mth.PI * t))));
            }
            b.ribbon(SunRenderTypes.RAY, path, body * 0.09F, body * 0.09F, FLAME, 0.8F * life * alpha, 0.8F * life * alpha, 0.0F);
            b.ribbon(SunRenderTypes.RAY, path, body * 0.035F, body * 0.035F, GOLD, 0.7F * life * alpha, 0.7F * life * alpha, 0.0F);
        }

        // flares
        for (int j = 0; j < 3; j++) {
            float clock = time / 70.0F + j * 0.41F;
            float cycle = Mth.frac(clock);
            float fade = 1.0F - Mth.clamp((cycle - 0.12F) / 0.5F, 0.0F, 1.0F);
            if (fade <= 0.0F) {
                continue;
            }
            int burst = Mth.floor(clock);
            Vec3 n = spot(Math.floorMod(burst * 13 + j * 7, 97), 97, turned, TILT);
            float facing = b.facing(c, n, body);
            if (facing < -0.3F || facing > 0.75F) {
                continue;
            }
            float grow = Math.min(1.0F, cycle / 0.12F);
            float from = body * 0.9F;
            float len = Math.min(body * 1.05F * grow, edge - from);
            b.tongue(c, n, from, len, body * 0.3F, burst + j, ORANGE, fade * alpha);
            b.tongue(c, n, from, len * 0.7F, body * 0.18F, burst + j + 2, PALE, 0.8F * fade * alpha);
        }
    }

    // i of count spread evenly over a ball, turning with the surface
    private static Vec3 spot(int i, int count, float turned, float tilt) {
        float y = 1.0F - 2.0F * (i + 0.5F) / count;
        float ring = Mth.sqrt(Math.max(0.0F, 1.0F - y * y));
        float a = i * 2.39996F + turned;
        float x = ring * Mth.cos(a);
        float z = ring * Mth.sin(a);
        float ct = Mth.cos(tilt);
        float st = Mth.sin(tilt);
        return new Vec3(x, y * ct - z * st, y * st + z * ct);
    }

    // birth: shaft from the sky, rings closing in on the ground, a column and six streams into the spark.
    // all light, none of it burns
    private static void birth(SunBatch b, Vec3 feet, Vec3 center, float full, float time, float ticks) {
        float in = Math.min(1.0F, ticks / 6.0F);
        float out = 1.0F - Mth.clamp((ticks - CruelSunEntity.IGNITE_TICKS - 6.0F) / 24.0F, 0.0F, 1.0F);
        float alpha = in * out;
        if (alpha <= 0.01F) {
            return;
        }
        float gather = Math.min(1.0F, ticks / CruelSunEntity.IGNITE_TICKS);
        float wide = Math.max(4.0F, full * 1.2F);
        Vec3 ground = new Vec3(center.x, feet.y + 0.08, center.z);

        // ground rings
        b.flatGlow(ground, wide * 1.2F, GOLD, 0.55F * alpha);
        b.flatGlow(ground, wide * 0.5F, WHITE, 0.5F * alpha * gather);
        for (int k = 0; k < 4; k++) {
            float cycle = Mth.frac(time * (0.05F + 0.04F * gather) + k * 0.25F);
            b.flatRing(ground.add(0, 0.02 * k, 0), wide * (1.0F - cycle * cycle), k % 2 == 0 ? ORANGE : GOLD,
                    0.9F * alpha * Mth.sin(Mth.PI * cycle));
        }

        // sky shaft + ground column
        Vec3[] shaft = {center, center.add(0, full * 4.5F, 0)};
        b.ribbon(SunRenderTypes.RAY, shaft, full * (0.25F + 0.45F * gather), full * 0.9F, GOLD, 0.8F * alpha, 0.0F, 0.0F);
        b.ribbon(SunRenderTypes.RAY, shaft, full * (0.08F + 0.16F * gather), full * 0.25F, WHITE, 0.9F * alpha, 0.0F, 0.0F);
        Vec3[] column = {ground, center};
        b.ribbon(SunRenderTypes.RAY, column, full * 0.3F, full * 0.15F, GOLD, 0.5F * alpha, 0.8F * alpha, 0.0F);
        b.ribbon(SunRenderTypes.RAY, column, full * 0.1F, full * 0.06F, WHITE, 0.7F * alpha, 0.9F * alpha, 0.0F);

        // streams
        for (int k = 0; k < 6; k++) {
            Vec3[] path = new Vec3[26];
            for (int j = 0; j < path.length; j++) {
                float t = j / (float) (path.length - 1);
                float a = k * Mth.TWO_PI / 6.0F + t * 6.5F - time * 0.3F;
                float r = wide * (1.0F - t);
                double y = Mth.lerp(t, ground.y, center.y) + Mth.sin(Mth.PI * t) * full * 1.2F;
                path[j] = new Vec3(center.x + Mth.cos(a) * r, y, center.z + Mth.sin(a) * r);
            }
            float thick = full * 0.22F;
            b.ribbon(SunRenderTypes.RAY, path, thick, thick * 0.3F, FLAME, 0.7F * alpha, alpha, 0.0F);
            b.ribbon(SunRenderTypes.RAY, path, thick * 0.5F, thick * 0.15F, GOLD, 0.8F * alpha, alpha, 0.0F);
            b.ribbon(SunRenderTypes.RAY, path, thick * 0.18F, thick * 0.06F, WHITE, 0.6F * alpha, alpha, 0.0F);
        }
    }

    // ignition flash
    private static void ignition(SunBatch b, Vec3 feet, Vec3 center, float full, float since) {
        if (since >= 16.0F) {
            return;
        }
        float k = since / 16.0F;
        float gone = (1.0F - k) * (1.0F - k);
        b.halo(center, full * (2.5F + 4.0F * k), FLAME, 0.5F * gone);
        b.halo(center, full * (1.6F + 3.0F * k), GOLD, 0.8F * gone);
        b.glow(center, full * (1.2F + 2.0F * k), WHITE, gone);
        Vec3 ground = new Vec3(center.x, feet.y + 0.1, center.z);
        Vec3[] shaft = {ground, center.add(0, full * 5.0F, 0)};
        b.ribbon(SunRenderTypes.RAY, shaft, full * 0.7F * (1.0F - k), full * 0.4F * (1.0F - k), WHITE, gone, 0.0F, 0.0F);
        // shell
        b.ball(true, AddonRenderTypes.WHITE, center, full * (0.3F + 2.6F * k), 0.0F, 0.0F, GOLD, GOLD, 0.0F, 0.8F * gone);
        b.flatRing(ground, full * (0.5F + 4.0F * k), PALE, gone);
        b.flatRing(ground.add(0, 0.03, 0), full * (0.3F + 3.0F * k), ORANGE, 0.8F * gone);
    }

    // god rays
    private static void rays(SunBatch b, Vec3 c, float start, float reach, float width, int count, float time, int[] color, float alpha) {
        for (int i = 0; i < count; i++) {
            float a = i * Mth.TWO_PI / count + time * 0.004F + (i % 3) * 0.13F;
            float len = reach * (0.55F + 0.45F * (0.5F + 0.5F * Mth.sin(time * 0.21F + i * 3.1F)));
            float fade = 0.6F + 0.4F * Mth.sin(time * 0.33F + i * 1.3F);
            b.ray(c, a, start, Math.max(start + 0.1F, len), width, color, alpha * fade);
        }
    }

    // ground light
    private static void ground(SunBatch b, Vec3 feet, float orb, float since, float sunLevel) {
        Vec3 at = feet.add(0, 0.05, 0);
        float grow = Math.min(1.0F, since / 20.0F);
        b.flatGlow(at, orb * 2.2F * grow, GOLD, 0.35F + 0.1F * sunLevel);
        for (int k = 0; k < 3; k++) {
            float cycle = ((since + k * 13.0F) % 40.0F) / 40.0F;
            b.flatRing(at.add(0, 0.02 * k, 0), orb * (0.4F + 1.8F * cycle) * grow, ORANGE, 0.75F * (1.0F - cycle));
        }
    }

    // trail, same taper as CruelSunEntity.TRAIL_*
    private static void trail(SunBatch b, Vec3 dir, float r, float time, Vec3 fromCamera) {
        if (dir.lengthSqr() < 1.0E-4) {
            return;
        }
        dir = dir.normalize();
        Vec3 w = dir.cross(fromCamera);
        w = w.lengthSqr() < 1.0E-6 ? b.right : w.normalize();
        Vec3 back = dir.scale(-r * CruelSunEntity.TRAIL_LENGTH);
        float wide = r * CruelSunEntity.TRAIL_WIDTH;
        float tip = r * CruelSunEntity.TRAIL_TIP;
        float scroll = time * 0.08F;
        b.streak(SunRenderTypes.FLAMES, Vec3.ZERO, back, w, wide, tip, ORANGE, 0.95F, scroll);
        b.streak(SunRenderTypes.FLAMES, Vec3.ZERO, back.scale(0.75), w, wide * 0.6F, tip, GOLD, 0.9F, scroll + 0.37F);
        b.streak(SunRenderTypes.RAY, Vec3.ZERO, back.scale(0.9), w, wide * 0.35F, tip * 0.5F, WHITE, 0.8F, 0.0F);
    }

    private static void blast(SunBatch b, CruelSunEntity entity, float partialTick, float time, float sunLevel, float detail) {
        float ticks = entity.getPhaseTicks(partialTick);
        float radius = entity.getBlastRadius();
        float front = CruelSunEntity.blastFront(radius, ticks);
        float fade = CruelSunEntity.blastFade(ticks);
        float swell = front * (1.0F + CruelSunEntity.FADE_SWELL * fade);
        float a = (float) Math.pow(1.0F - fade, 1.3);
        if (swell < 0.05F) {
            return;
        }
        // white -> orange -> red
        int[] shell = SunBatch.mix(ORANGE, RED, fade);
        int[] heart = SunBatch.mix(WHITE, GOLD, Math.min(1.0F, fade * 1.5F));
        float turned = time * 0.02F;
        float slide = -turned / Mth.PI;

        // flash
        if (ticks < 8.0F) {
            b.glow(Vec3.ZERO, radius * 2.4F, WHITE, 1.0F - ticks / 8.0F);
        }
        // ground glow outlasts the fire
        b.flatGlow(new Vec3(0, 0.06, 0), swell * 1.05F, FLAME, 0.7F * (1.0F - fade * fade));
        b.flatGlow(new Vec3(0, 0.08, 0), swell * 0.5F, GOLD, 0.5F * (1.0F - fade));
        // solid at first then thins out
        float solid = fade < 0.25F ? 1.0F : (float) Math.pow(Math.max(0.0F, 1.0F - (fade - 0.25F) / 0.4F), 1.5);

        if (a > 0.01F) {
            b.glow(Vec3.ZERO, swell * 1.8F, GOLD, 0.25F * a);
            b.glow(Vec3.ZERO, swell * 1.15F, shell, 0.3F * a);
            rays(b, Vec3.ZERO, swell * 0.3F, swell, swell * 0.08F, 18, time, PALE, 0.6F * a * (1.0F - fade));

            // dome, the skin sits on the damage front
            if (solid > 0.5F) {
                b.depthBall(Vec3.ZERO, swell * 0.95F);
            }
            b.ball(true, SunRenderTypes.PLASMA_A, Vec3.ZERO, swell * 0.97F, 0.3F, slide, SunBatch.mix(PALE, ORANGE, fade), SunBatch.mix(ORANGE, FLAME, fade), solid, solid);
            b.ball(false, SunRenderTypes.PLASMA_A, Vec3.ZERO, swell * 0.98F, 0.3F, slide, FLAME, FLAME, 0.5F * a, 0.7F * a);
            b.ball(false, SunRenderTypes.PLASMA_B, Vec3.ZERO, swell * 0.99F, -0.8F, -slide * 0.7F, GOLD, ORANGE,
                    0.45F * a * (1.0F - fade), 0.3F * a);
            // hotter ball inside
            b.ball(false, SunRenderTypes.PLASMA_B, Vec3.ZERO, swell * 0.6F, 0.5F, slide * 1.6F, heart, GOLD, 0.5F * a, 0.2F * a);
            // shock front
            b.ball(false, SunRenderTypes.PLASMA_B, Vec3.ZERO, swell, 1.1F, slide * 0.4F, PALE, PALE, 0.0F, 0.9F * a * (1.0F - fade));

            // tongues, tips on the damage front
            int flames = Math.max(24, (int) (70 * detail));
            for (int i = 0; i < flames; i++) {
                Vec3 n = spot(i, flames, turned * 0.3F, 0.3F);
                float facing = b.facing(Vec3.ZERO, n, swell);
                if (n.y < -0.15 || facing < -0.35F || facing > 0.3F) {
                    continue;
                }
                float flick = 0.5F + 0.5F * Mth.sin(time * 0.4F + i * 2.13F);
                // fade with the solid fire or they look like loose cones
                float show = a * solid * Mth.clamp(1.15F - facing, 0.0F, 1.0F);
                b.tongue(Vec3.ZERO, n, swell * 0.72F, swell * (0.14F + 0.14F * flick), swell * 0.12F, i, FLAME, 0.9F * show);
                b.tongue(Vec3.ZERO, n, swell * 0.72F, swell * (0.08F + 0.08F * flick), swell * 0.08F, i + 2, GOLD, 0.8F * show);
            }

            // ground shockwaves
            for (int k = 0; k < 3; k++) {
                float lag = Math.max(0.0F, 1.0F - k * 0.18F);
                b.flatRing(new Vec3(0, 0.1 * k, 0), swell * lag, k == 0 ? PALE : ORANGE, (0.9F - 0.2F * k) * a);
            }
        }

        // smoke, can go past the edge
        if (fade > 0.15F) {
            float k = (fade - 0.15F) / 0.85F;
            float smokeAlpha = 0.6F * Mth.sin(Math.min(1.0F, k * 1.3F) * Mth.PI) + 0.1F * (1.0F - k);
            for (int i = 0; i < 16; i++) {
                float ang = i * 2.399F;
                float dist = radius * (0.2F + 0.5F * ((i * 37) % 11) / 10.0F);
                Vec3 at = new Vec3(Mth.cos(ang) * dist, radius * (0.1F + 0.9F * k) * (0.5F + 0.5F * ((i * 13) % 7) / 6.0F),
                        Mth.sin(ang) * dist);
                float size = radius * (0.35F + 0.3F * ((i * 7) % 5) / 4.0F) * (0.8F + 0.6F * k);
                b.smoke(at, size, time * 0.01F + i, SMOKE, smokeAlpha);
            }
        }
    }
}
