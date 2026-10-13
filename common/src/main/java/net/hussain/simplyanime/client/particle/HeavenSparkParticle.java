package net.hussain.simplyanime.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class HeavenSparkParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    public HeavenSparkParticle(ClientLevel world, double x, double y, double z,
                                  double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.xd = velocityX + (this.random.nextDouble() - 0.5) * 0.05;
        this.yd = velocityY + (this.random.nextDouble() - 0.5) * 0.05;
        this.zd = velocityZ + (this.random.nextDouble() - 0.5) * 0.05;
        this.friction = 0.82F;
        this.gravity = 0.1F;
        this.quadSize = 0.08F + this.random.nextFloat() * 0.08F;
        this.lifetime = 6 + this.random.nextInt(6);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = 1.0F - (float) this.age / this.lifetime * 0.7F;
    }

    @Override
    public int getLightColor(float tint) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
