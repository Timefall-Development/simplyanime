package net.ghostedhype.simplyanime.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

// gold cooling to red, no gravity
public class RuptureEmberParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    public RuptureEmberParticle(ClientLevel world, double x, double y, double z,
                                   double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(world, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        this.friction = 0.9F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.12F;
        this.lifetime = 10 + this.random.nextInt(10);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        float life = (float) this.age / this.lifetime;
        this.gCol = 0.8F - life * 0.7F;
        this.bCol = 0.45F - life * 0.4F;
        this.alpha = 1.0F - life * life;
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
