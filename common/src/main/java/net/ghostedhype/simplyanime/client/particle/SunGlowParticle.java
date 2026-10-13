package net.ghostedhype.simplyanime.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ghostedhype.simplyanime.platform.ClientPlatform;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SunGlowParticle extends TextureSheetParticle {

    private final boolean spark;
    private final float startScale;

    private SunGlowParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz,
                            SpriteSet sprites, boolean spark) {
        super(world, x, y, z, 0, 0, 0);
        this.spark = spark;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.hasPhysics = false;
        if (spark) {
            this.gravity = 0.25F;
            this.friction = 0.95F;
            this.startScale = 0.05F + this.random.nextFloat() * 0.05F;
            this.lifetime = 14 + this.random.nextInt(14);
        } else {
            this.gravity = -0.01F;
            this.friction = 0.9F;
            this.startScale = 0.6F + this.random.nextFloat() * 0.9F;
            this.lifetime = 16 + this.random.nextInt(14);
        }
        this.quadSize = this.startScale;
        this.pickSprite(sprites);
        this.shade(0.0F);
    }

    public static SunGlowParticle spark(ClientLevel level,
                                        double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed,
                                        SpriteSet spriteSet) {
        return new SunGlowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet, true);
    }

    public static SunGlowParticle wisp(ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       SpriteSet spriteSet) {
        return new SunGlowParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet, false);
    }

    @Override
    public void tick() {
        super.tick();
        this.shade((float) this.age / this.lifetime);
    }

    // white -> gold -> red
    private void shade(float life) {
        this.rCol = 1.0F;
        if (this.spark) {
            this.gCol = Mth.clamp(0.95F - life * 0.75F, 0.2F, 1.0F);
            this.bCol = Mth.clamp(0.7F - life * 1.6F, 0.02F, 1.0F);
            this.alpha = 1.0F - life * life * life;
        } else {
            this.gCol = Mth.clamp(0.72F - life * 0.5F, 0.2F, 1.0F);
            this.bCol = Mth.clamp(0.25F - life * 0.4F, 0.02F, 1.0F);
            this.quadSize = this.startScale * (0.5F + 1.5F * life);
            this.alpha = 0.55F * Mth.sin(Mth.PI * Math.min(1.0F, life * 1.15F));
        }
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        if (!this.spark) {
            super.render(consumer, camera, partialTick);
            return;
        }
        // streak back along the velocity, facing the camera
        Vec3 cam = camera.getPosition();
        Vec3 at = new Vec3(Mth.lerp(partialTick, this.xo, this.x) - cam.x,
                Mth.lerp(partialTick, this.yo, this.y) - cam.y,
                Mth.lerp(partialTick, this.zo, this.z) - cam.z);
        Vec3 motion = new Vec3(this.xd, this.yd, this.zd);
        double speed = motion.length();
        if (speed < 1.0E-4) {
            super.render(consumer, camera, partialTick);
            return;
        }
        Vec3 dir = motion.scale(1.0 / speed);
        Vec3 side = dir.cross(at);
        if (side.lengthSqr() < 1.0E-8) {
            return;
        }
        side = side.normalize().scale(this.quadSize);
        Vec3 tail = at.subtract(dir.scale(this.quadSize * 2.0 + speed * 2.2));
        Vec3 head = at.add(dir.scale(this.quadSize));
        int light = 0xF000F0;
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        vertex(consumer, tail.subtract(side), u0, v1, light);
        vertex(consumer, tail.add(side), u1, v1, light);
        vertex(consumer, head.add(side), u1, v0, light);
        vertex(consumer, head.subtract(side), u0, v0, light);
    }

    private void vertex(VertexConsumer consumer, Vec3 p, float u, float v, int light) {
        ClientPlatform.particleVertex(consumer, p.x, p.y, p.z, u, v, this.rCol, this.gCol, this.bCol, this.alpha, light);
    }

    @Override
    public int getLightColor(float tint) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.spark ? ClientPlatform.ADDITIVE_PARTICLES : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
