package net.ghostedhype.simplyanime.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleParticleFactory implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;
    private final ParticleConstructor constructor;

    public SimpleParticleFactory(SpriteSet spriteSet, ParticleConstructor constructor) {
        this.spriteSet = spriteSet;
        this.constructor = constructor;
    }

    @Override
    public @Nullable Particle createParticle(@NotNull SimpleParticleType simpleParticleType, @NotNull ClientLevel clientLevel,
                                             double x, double y, double z,
                                             double velocityX, double velocityY, double velocityZ) {
        return this.constructor.create(clientLevel,
                x, y, z,
                velocityX, velocityY, velocityZ,
                this.spriteSet);
    }
}
