package net.ghostedhype.simplyanime.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ghostedhype.simplyanime.entity.EnumaElishVisualEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class EnumaElishVisualEntityRenderer extends EntityRenderer<EnumaElishVisualEntity> {

    private static final int[] WHITE = {255, 255, 244};
    private static final int[] GOLD = {255, 206, 120};
    private static final int[] ORANGE = {255, 64, 26};
    private static final int[] RED = {255, 34, 18};
    private static final int[] CRIMSON = {128, 4, 12};
    private static final int[] ABYSS = {34, 0, 6};

    private static final double STEP = 1.5;
    private static final int AROUND = 18;

    public EnumaElishVisualEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EnumaElishVisualEntity entity) {
        return AddonRenderTypes.WHITE;
    }

    @Override
    public boolean shouldRender(EnumaElishVisualEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(EnumaElishVisualEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light) {
        if (Later.take(entity, poseStack, (pose, late) -> this.render(entity, yaw, partialTick, pose, late, light))) {
            return;
        }
        // light doesn't cast shadows
        if (ShaderCompat.shadowPass()) {
            return;
        }
        Matrix4f matrix = poseStack.last().pose();
        Vec3 base = entity.getPosition(partialTick);
        Vec3 camera = this.entityRenderDispatcher.camera.getPosition().subtract(base);
        Quaternionf rotation = this.entityRenderDispatcher.cameraOrientation();
        Vec3 camRight = VfxBuffer.rotate(rotation, 1, 0, 0);
        Vec3 camUp = VfxBuffer.rotate(rotation, 0, 1, 0);
        float time = entity.tickCount + partialTick;
        float progress = entity.getPhaseProgress(partialTick);

        switch (entity.getPhase()) {
            case EnumaElishVisualEntity.PHASE_CHARGE -> {
                LivingEntity owner = entity.getOwner();
                if (owner != null) {
                    renderCharge(entity, owner, base, matrix, buffers, camRight, camUp, partialTick, time, progress);
                }
            }
            case EnumaElishVisualEntity.PHASE_RELEASE -> renderFlash(matrix, buffers, camRight, camUp, entity, progress);
            case EnumaElishVisualEntity.PHASE_BEAM -> {
                float grow = EnumaElishVisualEntity.growth(entity.getPhaseTicks(partialTick));
                renderBeam(entity, matrix, buffers, camera, camRight, camUp, time, grow, 0.0F);
            }
            case EnumaElishVisualEntity.PHASE_FADE -> renderBeam(entity, matrix, buffers, camera, camRight, camUp, time, 1.0F, progress);
            default -> {
            }
        }
    }

    private void renderCharge(EnumaElishVisualEntity entity, LivingEntity owner, Vec3 base, Matrix4f matrix,
                              MultiBufferSource buffers, Vec3 camRight, Vec3 camUp, float partialTick,
                              float time, float progress) {
        Vec3 tip = EnumaElishVisualEntity.chargeTip(owner, partialTick).subtract(base);
        Vec3 feet = owner.getPosition(partialTick).subtract(base).add(0, 0.06, 0);
        Vec3 axis = tip.subtract(feet.add(0, owner.getBbHeight() * 0.8, 0)).normalize();
        Vec3 u = EnumaElishVisualEntity.perpendicular(axis);
        Vec3 v = axis.cross(u);
        float rise = Math.min(1.0F, entity.getPhaseTicks(partialTick) / 10.0F);

        VfxBuffer shade = VfxBuffer.of(buffers, AddonRenderTypes.shade());
        for (int i = 0; i < 3; i++) {
            Vec3 center = tip.subtract(axis.scale(0.35 + i * 0.5));
            float radius = (0.5F + i * 0.28F) * (0.6F + 0.7F * progress) * rise;
            drawSpinningBand(shade, matrix, center, axis, u, v, radius * 1.08F, 0.09F, CRIMSON, 0.55F * rise,
                    time * (0.35F + i * 0.1F), 0.4F);
        }

        VfxBuffer glow = VfxBuffer.of(buffers, AddonRenderTypes.glow());
        for (int i = 0; i < 3; i++) {
            Vec3 center = tip.subtract(axis.scale(0.35 + i * 0.5));
            float radius = (0.5F + i * 0.28F) * (0.6F + 0.7F * progress) * rise;
            drawSpinningBand(glow, matrix, center, axis, u, v, radius, 0.07F, RED, 0.9F * rise, -time * (0.4F + i * 0.12F), 0.3F);
        }

        // orb
        float orb = (0.25F + 1.5F * progress * progress) * rise;
        int[] heart = progress > 0.6F ? WHITE : GOLD;
        drawBillboardGlow(glow, matrix, tip, camRight, camUp, orb * 2.6F, RED, 0.55F);
        drawBillboardGlow(glow, matrix, tip, camRight, camUp, orb * 1.4F, ORANGE, 0.8F);
        drawBillboardGlow(glow, matrix, tip, camRight, camUp, orb * 0.7F, heart, 1.0F);

        // ground sigil
        Vec3 upward = new Vec3(0, 1, 0);
        Vec3 east = new Vec3(1, 0, 0);
        Vec3 south = new Vec3(0, 0, 1);
        float sigil = (2.0F + 1.2F * progress) * rise;
        drawFlatRing(glow, matrix, feet, east, south, sigil, 0.14F, RED, 0.75F * rise, time * 0.08F, 10);
        drawFlatRing(glow, matrix, feet, east, south, sigil * 0.62F, 0.1F, ORANGE, 0.6F * rise, -time * 0.12F, 6);
        drawDisc(glow, matrix, feet.add(upward.scale(0.01)), east, south, sigil * 1.1F, RED, 0.25F * progress, 0.0F);
    }

    private void renderFlash(Matrix4f matrix, MultiBufferSource buffers, Vec3 camRight, Vec3 camUp,
                             EnumaElishVisualEntity entity, float progress) {
        VfxBuffer glow = VfxBuffer.of(buffers, AddonRenderTypes.glow());
        float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
        float radius = Math.min(entity.getRadius(), 4.0F);
        Vec3 position = Vec3.ZERO;
        drawBillboardGlow(glow, matrix, position, camRight, camUp, radius * (1.5F + 3.5F * eased), GOLD, 0.9F);
        drawBillboardGlow(glow, matrix, position, camRight, camUp, radius * (0.8F + 1.6F * eased), WHITE, 1.0F);
        // star streaks off the flash
        for (int i = 0; i < 8; i++) {
            float a = i * Mth.TWO_PI / 8 + 0.2F;
            Vec3 direction = camRight.scale(Mth.cos(a)).add(camUp.scale(Mth.sin(a)));
            Vec3 side = camRight.scale(-Mth.sin(a)).add(camUp.scale(Mth.cos(a)));
            float spikeLength = radius * (2.0F + 4.0F * eased) * (i % 2 == 0 ? 1.0F : 0.6F);
            drawSpike(glow, matrix, position, direction, side, spikeLength, 0.18F * radius, WHITE, 0.9F);
        }
    }

    private void renderBeam(EnumaElishVisualEntity entity, Matrix4f matrix, MultiBufferSource buffers,
                            Vec3 camera, Vec3 camRight, Vec3 camUp, float time, float grow, float fade) {
        Vec3 direction = entity.getAim();
        if (direction.lengthSqr() < 1.0E-4) {
            return;
        }
        direction = direction.normalize();
        Vec3 u = EnumaElishVisualEntity.perpendicular(direction);
        Vec3 v = direction.cross(u);
        float fullRadius = entity.getRadius();
        float thin = EnumaElishVisualEntity.fadeThickness(fade);
        float alpha = 1.0F - fade;
        double length = entity.getLength() * grow;
        if (length < 0.2) {
            return;
        }
        int segments = Math.max(1, (int) Math.ceil(length / STEP));

        // shells fade when the camera is inside the beam
        Vec3 toCam = camera;
        double along = Mth.clamp(toCam.dot(direction), 0.0, length);
        double axisDistance = toCam.subtract(direction.scale(along)).length();
        float inside = (float) Mth.clamp(axisDistance / (fullRadius * 1.3), 0.3, 1.0);

        if (ShaderCompat.packInUse()) {
            VfxBuffer depth = VfxBuffer.of(buffers, AddonRenderTypes.depth(AddonRenderTypes.WHITE));
            for (int i = 0; i < segments; i++) {
                double startDistance = length * i / segments;
                double endDistance = length * (i + 1) / segments;
                Vec3 startPoint = direction.scale(startDistance);
                Vec3 endPoint = direction.scale(endDistance);
                Vec3 startSide = facing(direction, startPoint, camera, u);
                Vec3 endSide = facing(direction, endPoint, camera, u);
                Vec3 startBack = startPoint.subtract(camera.subtract(startPoint).normalize().scale(0.3));
                Vec3 endBack = endPoint.subtract(camera.subtract(endPoint).normalize().scale(0.3));
                drawRibbon(depth, matrix, startBack, endBack, startSide, endSide, radius(startDistance, time, fullRadius) * thin * 0.4F,
                        radius(endDistance, time, fullRadius) * thin * 0.4F, WHITE, 1.0F, 1.0F);
            }
        }
        VfxBuffer shade = VfxBuffer.of(buffers, AddonRenderTypes.shade());
        for (int k = 0; k < 4; k++) {
            int[] color = k % 2 == 0 ? ABYSS : CRIMSON;
            float strandAlpha = (k % 2 == 0 ? 0.7F : 0.55F) * alpha * inside;
            drawSwirl(shade, matrix, direction, u, v, camera, length, fullRadius, thin, time, k * Mth.HALF_PI, color, strandAlpha);
        }

        VfxBuffer glow = VfxBuffer.of(buffers, AddonRenderTypes.glow());

        // ribbons for the bloom
        for (int i = 0; i < segments; i++) {
            double startDistance = length * i / segments;
            double endDistance = length * (i + 1) / segments;
            Vec3 startPoint = direction.scale(startDistance);
            Vec3 endPoint = direction.scale(endDistance);
            Vec3 startSide = facing(direction, startPoint, camera, u);
            Vec3 endSide = facing(direction, endPoint, camera, u);
            float edgeOn0 = sideOn(direction, startPoint, camera);
            float edgeOn1 = sideOn(direction, endPoint, camera);
            float startRadius = radius(startDistance, time, fullRadius) * thin;
            float endRadius = radius(endDistance, time, fullRadius) * thin;
            float tipFade0 = tipFade(startDistance, length);
            float tipFade1 = tipFade(endDistance, length);
            drawRibbon(glow, matrix, startPoint, endPoint, startSide, endSide, startRadius * 2.4F, endRadius * 2.4F, RED, 0.6F * alpha * edgeOn0 * tipFade0, 0.6F * alpha * edgeOn1 * tipFade1);
            drawRibbon(glow, matrix, startPoint, endPoint, startSide, endSide, startRadius * 1.3F, endRadius * 1.3F, RED, 0.8F * alpha * edgeOn0 * tipFade0, 0.8F * alpha * edgeOn1 * tipFade1);
            drawRibbon(glow, matrix, startPoint, endPoint, startSide, endSide, startRadius * 0.7F, endRadius * 0.7F, ORANGE, 0.7F * alpha * edgeOn0, 0.7F * alpha * edgeOn1);
            drawRibbon(glow, matrix, startPoint, endPoint, startSide, endSide, startRadius * 0.4F, endRadius * 0.4F, GOLD, 0.8F * alpha * edgeOn0, 0.8F * alpha * edgeOn1);
            drawRibbon(glow, matrix, startPoint, endPoint, startSide, endSide, startRadius * 0.18F, endRadius * 0.18F, WHITE, alpha * edgeOn0, alpha * edgeOn1);
        }

        drawTube(glow, matrix, direction, u, v, length, fullRadius, thin, time, 0.95F, RED, 0.22F * alpha * inside);
        drawTube(glow, matrix, direction, u, v, length, fullRadius, thin, time, 0.55F, RED, 0.2F * alpha * inside);
        drawTube(glow, matrix, direction, u, v, length, fullRadius, thin, time, 0.22F, GOLD, 0.3F * alpha);

        // muzzle flare + rupture rings
        Vec3 muzzle = direction.scale(0.2);
        float pulse = 1.0F + 0.08F * Mth.sin(time * 1.7F);
        float muzzleSize = EnumaElishVisualEntity.muzzleRadius(fullRadius);
        drawDisc(glow, matrix, muzzle, u, v, muzzleSize * pulse * thin, GOLD, 0.9F * alpha, 0.0F);
        drawBillboardGlow(glow, matrix, muzzle, camRight, camUp, muzzleSize * 0.5F * pulse * thin, WHITE, 0.85F * alpha);
        for (int i = 0; i < 3; i++) {
            double ringRadius = fullRadius * (0.5 + i * 0.8);
            if (ringRadius > length) {
                break;
            }
            float r = radius(ringRadius, time, fullRadius) * 1.45F * thin * pulse;
            drawSpinningBand(glow, matrix, direction.scale(ringRadius), direction, u, v, r, 0.05F + r * 0.02F, RED, 0.9F * alpha,
                    time * (i % 2 == 0 ? 0.3F : -0.22F), 0.35F);
        }

        // shock rings
        for (int k = 0; k < 12; k++) {
            float age = (time % 5.0F) + k * 5.0F;
            double spin = age * 4.5;
            if (spin > length) {
                continue;
            }
            float r = radius(spin, time, fullRadius) * 1.3F * thin;
            float ringAlpha = 0.85F * alpha * (1.0F - (float) (spin / Math.max(length, 1.0)));
            drawBand(glow, matrix, direction.scale(spin), direction, u, v, r, 0.12F + r * 0.03F, RED, ringAlpha);
        }

        // beam front
        Vec3 end = direction.scale(length);
        if (grow < 1.0F) {
            drawBillboardGlow(glow, matrix, end, camRight, camUp, fullRadius * 1.5F, WHITE, 0.9F);
        }

        // ground shockwave
        float wave = entity.getPhase() == EnumaElishVisualEntity.PHASE_BEAM
                ? Math.min(1.0F, entity.getPhaseTicks(0.0F) / 14.0F) : 1.0F;
        if (wave < 1.0F) {
            Vec3 ground = direction.scale(-1.6).add(0, -1.3, 0);
            Vec3 east = new Vec3(1, 0, 0);
            Vec3 south = new Vec3(0, 0, 1);
            float spread = fullRadius * 3.0F * (1.0F - (1.0F - wave) * (1.0F - wave));
            drawFlatRing(glow, matrix, ground, east, south, spread, 0.35F + spread * 0.03F, ORANGE, 0.9F * (1.0F - wave),
                    time * 0.02F, 16);
            drawDisc(glow, matrix, ground.add(0, 0.02, 0), east, south, spread, RED, 0.35F * (1.0F - wave), 0.0F);
        }

        if (entity.isHitTerrain() && grow >= 1.0F) {
            float blastAlpha = alpha * (0.85F + 0.15F * Mth.sin(time * 2.1F));
            // toned down up close or the whole screen goes white
            float near = (float) Mth.clamp(end.distanceTo(camera) / (fullRadius * 4.0), 0.35, 1.0);
            float size = EnumaElishVisualEntity.impactSize(fullRadius, fade);
            drawBillboardGlow(glow, matrix, end, camRight, camUp, size * 2.6F, RED, 0.75F * blastAlpha);
            drawBillboardGlow(glow, matrix, end, camRight, camUp, size * 1.4F, ORANGE, 0.8F * blastAlpha * near);
            drawBillboardGlow(glow, matrix, end, camRight, camUp, size * 0.6F, WHITE, 0.9F * blastAlpha * near);
        }

        // threads
        for (int k = 0; k < 3; k++) {
            drawThread(glow, matrix, direction, u, v, length, fullRadius, thin, time, k * Mth.TWO_PI / 3 + 0.5F, RED, 0.8F * alpha);
        }
    }

    private static float radius(double distance, float time, float full) {
        float ripple = 1.0F + 0.07F * Mth.sin((float) distance * 0.35F - time * 1.6F);
        return EnumaElishVisualEntity.profile(distance, full) * ripple;
    }

    private static float tipFade(double distance, double length) {
        return (float) Mth.clamp((length - distance) / 2.5 + 0.35, 0.35, 1.0);
    }

    // sideways vector that keeps a ribbon facing the camera
    private static Vec3 facing(Vec3 direction, Vec3 point, Vec3 camera, Vec3 fallback) {
        Vec3 side = direction.cross(point.subtract(camera));
        return side.lengthSqr() < 1.0E-6 ? fallback : side.normalize();
    }

    private static float sideOn(Vec3 direction, Vec3 point, Vec3 camera) {
        Vec3 view = point.subtract(camera);
        double viewLength = view.length();
        if (viewLength < 1.0E-4) {
            return 0.0F;
        }
        return (float) Mth.clamp(direction.cross(view.scale(1.0 / viewLength)).length() * 1.4, 0.0, 1.0);
    }

    private static void drawRibbon(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 startPoint, Vec3 endPoint, Vec3 startSide, Vec3 endSide,
                               float half0, float half1, int[] color, float startAlpha, float endAlpha) {
        if (startAlpha <= 0.01F && endAlpha <= 0.01F) {
            return;
        }
        Vec3 startLeft = startPoint.add(startSide.scale(-half0));
        Vec3 startRight = startPoint.add(startSide.scale(half0));
        Vec3 endLeft = endPoint.add(endSide.scale(-half1));
        Vec3 endRight = endPoint.add(endSide.scale(half1));
        vertex(vertexBuffer, matrix, startLeft, color, 0.0F);
        vertex(vertexBuffer, matrix, startPoint, color, startAlpha);
        vertex(vertexBuffer, matrix, endPoint, color, endAlpha);
        vertex(vertexBuffer, matrix, endLeft, color, 0.0F);

        vertex(vertexBuffer, matrix, startPoint, color, startAlpha);
        vertex(vertexBuffer, matrix, startRight, color, 0.0F);
        vertex(vertexBuffer, matrix, endRight, color, 0.0F);
        vertex(vertexBuffer, matrix, endPoint, color, endAlpha);
    }

    private static void drawTube(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 direction, Vec3 u, Vec3 v, double length, float full,
                             float thin, float time, float scale, int[] color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }
        int segments = Math.max(1, (int) Math.ceil(length / STEP));
        for (int i = 0; i < segments; i++) {
            double startDistance = length * i / segments;
            double endDistance = length * (i + 1) / segments;
            float startRadius = radius(startDistance, time, full) * scale * thin;
            float endRadius = radius(endDistance, time, full) * scale * thin;
            for (int j = 0; j < AROUND; j++) {
                float startAngle = j * Mth.TWO_PI / AROUND;
                float endAngle = (j + 1) * Mth.TWO_PI / AROUND;
                Vec3 startOffset = u.scale(Mth.cos(startAngle)).add(v.scale(Mth.sin(startAngle)));
                Vec3 endOffset = u.scale(Mth.cos(endAngle)).add(v.scale(Mth.sin(endAngle)));
                vertex(vertexBuffer, matrix, direction.scale(startDistance).add(startOffset.scale(startRadius)), color, alpha);
                vertex(vertexBuffer, matrix, direction.scale(startDistance).add(endOffset.scale(startRadius)), color, alpha);
                vertex(vertexBuffer, matrix, direction.scale(endDistance).add(endOffset.scale(endRadius)), color, alpha);
                vertex(vertexBuffer, matrix, direction.scale(endDistance).add(startOffset.scale(endRadius)), color, alpha);
            }
        }
    }

    private static void drawSwirl(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 direction, Vec3 u, Vec3 v, Vec3 camera, double length,
                              float full, float thin, float time, float offset, int[] color, float alpha) {
        int segments = Math.max(1, (int) Math.ceil(length / 0.75));
        float width = 0.34F;
        for (int i = 0; i < segments; i++) {
            double startDistance = length * i / segments;
            double endDistance = length * (i + 1) / segments;
            float startAngle = (float) startDistance * 0.42F + offset + time * 0.5F;
            float endAngle = (float) endDistance * 0.42F + offset + time * 0.5F;
            float startRadius = radius(startDistance, time, full) * 1.04F * thin;
            float endRadius = radius(endDistance, time, full) * 1.04F * thin;
            Vec3 startOffset = u.scale(Mth.cos(startAngle)).add(v.scale(Mth.sin(startAngle)));
            Vec3 endOffset = u.scale(Mth.cos(endAngle)).add(v.scale(Mth.sin(endAngle)));
            Vec3 mid = direction.scale(startDistance).add(startOffset.scale(startRadius));
            if (startOffset.dot(camera.subtract(mid)) <= 0) {
                continue;
            }
            Vec3 startOuter = u.scale(Mth.cos(startAngle + width)).add(v.scale(Mth.sin(startAngle + width)));
            Vec3 endOuter = u.scale(Mth.cos(endAngle + width)).add(v.scale(Mth.sin(endAngle + width)));
            float fade = tipFade(startDistance, length);
            vertex(vertexBuffer, matrix, direction.scale(startDistance).add(startOffset.scale(startRadius)), color, alpha * fade);
            vertex(vertexBuffer, matrix, direction.scale(startDistance).add(startOuter.scale(startRadius)), color, alpha * fade);
            vertex(vertexBuffer, matrix, direction.scale(endDistance).add(endOuter.scale(endRadius)), color, alpha * fade);
            vertex(vertexBuffer, matrix, direction.scale(endDistance).add(endOffset.scale(endRadius)), color, alpha * fade);
        }
    }

    private static void drawThread(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 direction, Vec3 u, Vec3 v, double length, float full,
                               float thin, float time, float offset, int[] color, float alpha) {
        int segments = Math.max(1, (int) Math.ceil(length / 0.75));
        for (int i = 0; i < segments; i++) {
            double startDistance = length * i / segments;
            double endDistance = length * (i + 1) / segments;
            float startAngle = (float) startDistance * -0.3F + offset - time * 0.35F;
            float endAngle = (float) endDistance * -0.3F + offset - time * 0.35F;
            float startRadius = radius(startDistance, time, full) * 1.12F * thin;
            float endRadius = radius(endDistance, time, full) * 1.12F * thin;
            Vec3 startOffset = u.scale(Mth.cos(startAngle)).add(v.scale(Mth.sin(startAngle)));
            Vec3 endOffset = u.scale(Mth.cos(endAngle)).add(v.scale(Mth.sin(endAngle)));
            Vec3 startPoint = direction.scale(startDistance).add(startOffset.scale(startRadius));
            Vec3 endPoint = direction.scale(endDistance).add(endOffset.scale(endRadius));
            Vec3 sideOffset = startOffset.scale(0.08 + startRadius * 0.02);
            float fade = tipFade(startDistance, length);
            vertex(vertexBuffer, matrix, startPoint.subtract(sideOffset), color, alpha * fade);
            vertex(vertexBuffer, matrix, startPoint.add(sideOffset), color, alpha * fade);
            vertex(vertexBuffer, matrix, endPoint.add(sideOffset), color, alpha * fade);
            vertex(vertexBuffer, matrix, endPoint.subtract(sideOffset), color, alpha * fade);
        }
    }

    // short open cylinder round an axis
    private static void drawBand(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 center, Vec3 axis, Vec3 u, Vec3 v, float radius,
                             float halfWidth, int[] color, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }
        Vec3 back = axis.scale(-halfWidth);
        Vec3 front = axis.scale(halfWidth);
        for (int j = 0; j < AROUND * 2; j++) {
            float startAngle = j * Mth.TWO_PI / (AROUND * 2);
            float endAngle = (j + 1) * Mth.TWO_PI / (AROUND * 2);
            Vec3 startOffset = u.scale(Mth.cos(startAngle) * radius).add(v.scale(Mth.sin(startAngle) * radius));
            Vec3 endOffset = u.scale(Mth.cos(endAngle) * radius).add(v.scale(Mth.sin(endAngle) * radius));
            vertex(vertexBuffer, matrix, center.add(startOffset).add(back), color, alpha);
            vertex(vertexBuffer, matrix, center.add(endOffset).add(back), color, alpha);
            vertex(vertexBuffer, matrix, center.add(endOffset).add(front), color, alpha);
            vertex(vertexBuffer, matrix, center.add(startOffset).add(front), color, alpha);
        }
    }

    private static void drawSpinningBand(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 center, Vec3 axis, Vec3 u, Vec3 v,
                                     float radius, float halfWidth, int[] color, float alpha, float spin, float floor) {
        if (alpha <= 0.01F || radius <= 0.01F) {
            return;
        }
        Vec3 back = axis.scale(-halfWidth);
        Vec3 front = axis.scale(halfWidth);
        int steps = AROUND * 2;
        for (int j = 0; j < steps; j++) {
            float startAngle = j * Mth.TWO_PI / steps;
            float endAngle = (j + 1) * Mth.TWO_PI / steps;
            float startBrightness = floor + (1.0F - floor) * Mth.frac(j / (float) steps + spin);
            float endBrightness = floor + (1.0F - floor) * Mth.frac((j + 1) / (float) steps + spin);
            if (endBrightness < startBrightness) {
                endBrightness = 1.0F;
            }
            Vec3 startOffset = u.scale(Mth.cos(startAngle) * radius).add(v.scale(Mth.sin(startAngle) * radius));
            Vec3 endOffset = u.scale(Mth.cos(endAngle) * radius).add(v.scale(Mth.sin(endAngle) * radius));
            vertex(vertexBuffer, matrix, center.add(startOffset).add(back), color, alpha * startBrightness);
            vertex(vertexBuffer, matrix, center.add(endOffset).add(back), color, alpha * endBrightness);
            vertex(vertexBuffer, matrix, center.add(endOffset).add(front), color, alpha * endBrightness);
            vertex(vertexBuffer, matrix, center.add(startOffset).add(front), color, alpha * startBrightness);
        }
    }

    // flat dashed ring lying in the u/v plane
    private static void drawFlatRing(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 center, Vec3 u, Vec3 v, float radius,
                                 float width, int[] color, float alpha, float spin, int dashes) {
        if (alpha <= 0.01F || radius <= 0.01F) {
            return;
        }
        int steps = dashes * 6;
        for (int j = 0; j < steps; j++) {
            if (j % 6 == 5) {
                continue;
            }
            float startAngle = j * Mth.TWO_PI / steps + spin;
            float endAngle = (j + 1) * Mth.TWO_PI / steps + spin;
            Vec3 startDirection = u.scale(Mth.cos(startAngle)).add(v.scale(Mth.sin(startAngle)));
            Vec3 endDirection = u.scale(Mth.cos(endAngle)).add(v.scale(Mth.sin(endAngle)));
            vertex(vertexBuffer, matrix, center.add(startDirection.scale(radius - width)), color, alpha);
            vertex(vertexBuffer, matrix, center.add(endDirection.scale(radius - width)), color, alpha);
            vertex(vertexBuffer, matrix, center.add(endDirection.scale(radius + width)), color, alpha);
            vertex(vertexBuffer, matrix, center.add(startDirection.scale(radius + width)), color, alpha);
        }
    }

    // filled circle
    private static void drawDisc(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 center, Vec3 u, Vec3 v, float radius,
                             int[] color, float alpha, float edgeAlpha) {
        if (alpha <= 0.01F || radius <= 0.01F) {
            return;
        }
        int steps = 24;
        for (int j = 0; j < steps; j++) {
            float startAngle = j * Mth.TWO_PI / steps;
            float endAngle = (j + 1) * Mth.TWO_PI / steps;
            Vec3 startPoint = center.add(u.scale(Mth.cos(startAngle) * radius)).add(v.scale(Mth.sin(startAngle) * radius));
            Vec3 endPoint = center.add(u.scale(Mth.cos(endAngle) * radius)).add(v.scale(Mth.sin(endAngle) * radius));
            vertex(vertexBuffer, matrix, center, color, alpha);
            vertex(vertexBuffer, matrix, center, color, alpha);
            vertex(vertexBuffer, matrix, endPoint, color, edgeAlpha);
            vertex(vertexBuffer, matrix, startPoint, color, edgeAlpha);
        }
    }

    private static void drawBillboardGlow(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 center, Vec3 camRight, Vec3 camUp,
                                      float radius, int[] color, float alpha) {
        drawDisc(vertexBuffer, matrix, center, camRight, camUp, radius, color, alpha, 0.0F);
    }

    private static void drawSpike(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 from, Vec3 direction, Vec3 side, float length,
                              float width, int[] color, float alpha) {
        Vec3 tip = from.add(direction.scale(length));
        vertex(vertexBuffer, matrix, from.add(side.scale(width)), color, alpha);
        vertex(vertexBuffer, matrix, from.subtract(side.scale(width)), color, alpha);
        vertex(vertexBuffer, matrix, tip, color, 0.0F);
        vertex(vertexBuffer, matrix, tip, color, 0.0F);
    }

    private static void vertex(VfxBuffer vertexBuffer, Matrix4f matrix, Vec3 point, int[] color, float alpha) {
        vertexBuffer.vertex(matrix, (float) point.x, (float) point.y, (float) point.z, color[0], color[1], color[2],
                (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255), 0.5F, 0.5F);
    }
}
