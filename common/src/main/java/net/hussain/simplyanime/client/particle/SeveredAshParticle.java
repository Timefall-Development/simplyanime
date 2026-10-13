package net.hussain.simplyanime.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SeveredAshParticle extends TextureSheetParticle {

    public SeveredAshParticle(ClientLevel world, double x, double y, double z,
                                 double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.friction = 0.88F;
        this.gravity = -0.02F;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.12F;
        this.lifetime = 14 + this.random.nextInt(10);
        this.roll = this.random.nextFloat() * 6.28F;
        this.oRoll = this.roll;
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += 0.08F;
        this.alpha = 1.0F - (float) this.age / this.lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
