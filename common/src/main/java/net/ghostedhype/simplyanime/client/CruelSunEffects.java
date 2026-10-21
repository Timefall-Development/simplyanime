package net.ghostedhype.simplyanime.client;

import net.ghostedhype.simplyanime.ClientHooks;
import net.ghostedhype.simplyanime.ability.SunPower;
import net.ghostedhype.simplyanime.client.sound.SunFlightSound;
import net.ghostedhype.simplyanime.config.RhittaClientConfig;
import net.ghostedhype.simplyanime.entity.CruelSunEntity;
import net.ghostedhype.simplyanime.registry.ParticlesRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CruelSunEffects implements ClientHooks {

    @Override
    public float localSunPower() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null || client.level == null ? -1.0F : SunPower.sunPower(client.level, client.player);
    }

    @Override
    public void sunFlightStarted(CruelSunEntity sun) {
        Minecraft.getInstance().getSoundManager().play(new SunFlightSound(sun));
    }

    private static float detail(Vec3 point) {
        Minecraft client = Minecraft.getInstance();
        float density = RhittaClientConfig.get().particleDensity;
        if (density <= 0 || client.gameRenderer == null) {
            return 0.0F;
        }
        double dist = client.gameRenderer.getMainCamera().getPosition().distanceTo(point);
        float far = dist < 32 ? 1.0F : dist < 96 ? 0.5F : 0.2F;
        return density * far;
    }

    private static int count(float amount, RandomSource random) {
        int count = (int) amount;
        return count + (random.nextFloat() < amount - count ? 1 : 0);
    }

    @Override
    public void sunTick(CruelSunEntity sun, LivingEntity owner) {
        Level world = sun.level();
        RandomSource random = world.getRandom();
        float heat = 0.5F + sun.getSun();
        switch (sun.getPhase()) {
            case CruelSunEntity.PHASE_CHARGE -> {
                if (owner == null) {
                    return;
                }
                float orb = sun.getOrbRadius(0.0F);
                Vec3 center = CruelSunEntity.chargeCenter(owner, 1.0F, orb);
                float detail = detail(center);
                float progress = sun.getPhaseProgress(0.0F);
                // embers spiral in
                for (int i = count((2 + 5 * progress) * heat * detail, random); i > 0; i--) {
                    double yaw = random.nextDouble() * Mth.TWO_PI;
                    double pitch = (random.nextDouble() - 0.5) * Math.PI;
                    double dist = orb * 2.5 + 2.0 + random.nextDouble() * 2.5;
                    Vec3 out = new Vec3(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch));
                    Vec3 from = center.add(out.scale(dist));
                    Vec3 swirl = out.cross(new Vec3(0, 1, 0)).scale(0.08);
                    Vec3 pull = center.subtract(from).scale(0.1).add(swirl);
                    world.addParticle(ParticlesRegistry.SUN_SPARK.get(), from.x, from.y, from.z, pull.x, pull.y, pull.z);
                }
                float charge = sun.getPhaseTicks(0.0F);
                float full = sun.getOrbBase();
                if (charge < CruelSunEntity.IGNITE_TICKS + 20) {
                    // ground sparks up the column
                    double wide = Math.max(4.0, full * 1.4);
                    for (int i = count((10 + full) * detail, random); i > 0; i--) {
                        double angle = random.nextDouble() * Mth.TWO_PI;
                        double r = wide * (0.3 + 0.7 * random.nextDouble());
                        Vec3 from = new Vec3(center.x + Math.cos(angle) * r, owner.getY() + 0.1, center.z + Math.sin(angle) * r);
                        Vec3 inward = center.subtract(from).normalize();
                        Vec3 swirl = new Vec3(-Math.sin(angle), 0, Math.cos(angle)).scale(0.35);
                        Vec3 velocity = inward.scale(0.5 + random.nextDouble() * 0.5).add(swirl).add(0, 0.35, 0);
                        world.addParticle(ParticlesRegistry.SUN_SPARK.get(), from.x, from.y, from.z, velocity.x, velocity.y, velocity.z);
                    }
                }
                if (charge < CruelSunEntity.IGNITE_TICKS) {
                    return;
                }
                if ((int) charge == CruelSunEntity.IGNITE_TICKS) {
                    // ignition burst
                    for (int i = count(140 * detail, random); i > 0; i--) {
                        Vec3 out = scatter(random, 1.0).normalize();
                        Vec3 velocity = out.scale(0.6 + random.nextDouble() * 1.4);
                        world.addParticle(ParticlesRegistry.SUN_SPARK.get(), center.x, center.y, center.z, velocity.x, velocity.y, velocity.z);
                    }
                    for (int i = count(30 * detail, random); i > 0; i--) {
                        Vec3 out = scatter(random, 1.0).normalize();
                        Vec3 point = center.add(out.scale(full * 0.3));
                        world.addParticle(ParticlesRegistry.SUN_WISP.get(), point.x, point.y, point.z, out.x * 0.5, out.y * 0.5, out.z * 0.5);
                    }
                }
                flicker(world, random, center, orb, (2.0F + orb) * heat * detail);
                // ground debris
                for (int i = count((1.0F + orb * 0.4F) * detail, random); i > 0; i--) {
                    double angle = random.nextDouble() * Mth.TWO_PI;
                    double r = 1.0 + random.nextDouble() * orb * 1.6;
                    BlockPos ground = BlockPos.containing(owner.getX() + Math.cos(angle) * r, owner.getY() - 0.5, owner.getZ() + Math.sin(angle) * r);
                    BlockState state = world.getBlockState(ground);
                    if (state.isAir()) {
                        continue;
                    }
                    world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), ground.getX() + 0.5,
                            ground.getY() + 1.05, ground.getZ() + 0.5, 0, 0.3 + random.nextDouble() * 0.5, 0);
                }
                if (random.nextFloat() < 0.5F * detail) {
                    double angle = random.nextDouble() * Mth.TWO_PI;
                    double r = 0.8 + random.nextDouble() * orb * 1.8;
                    world.addParticle(ParticleTypes.LARGE_SMOKE, owner.getX() + Math.cos(angle) * r, owner.getY() + 0.1,
                            owner.getZ() + Math.sin(angle) * r, 0, 0.05 + 0.05 * progress, 0);
                }
            }
            case CruelSunEntity.PHASE_FLIGHT -> {
                float orb = sun.getOrbRadius(0.0F);
                Vec3 sunCenter = sun.position();
                Vec3 direction = sun.getAim();
                float detail = detail(sunCenter);
                // trail
                for (int i = count(4 * heat * detail, random); i > 0; i--) {
                    Vec3 point = sunCenter.subtract(direction.scale(orb * 0.6)).add(scatter(random, orb * 0.7));
                    Vec3 velocity = direction.scale(-0.05).add(scatter(random, 0.03));
                    world.addParticle(ParticlesRegistry.SUN_WISP.get(), point.x, point.y, point.z, velocity.x, velocity.y, velocity.z);
                }
                flicker(world, random, sunCenter, orb, 1.0F * heat * detail);
                for (int i = count(6 * heat * detail, random); i > 0; i--) {
                    Vec3 point = sunCenter.add(scatter(random, orb * 0.8));
                    Vec3 velocity = direction.scale(-0.5 - random.nextDouble() * 0.6).add(scatter(random, 0.15));
                    world.addParticle(ParticlesRegistry.SUN_SPARK.get(), point.x, point.y, point.z, velocity.x, velocity.y, velocity.z);
                }
            }
            case CruelSunEntity.PHASE_BLAST -> {
                float ticks = sun.getPhaseTicks(0.0F);
                float radius = sun.getBlastRadius();
                Vec3 sunCenter = sun.position();
                float detail = detail(sunCenter);
                if (ticks < 2) {
                    // debris
                    for (int i = count(Math.min(120, radius * 5.0F) * detail, random); i > 0; i--) {
                        double angle = random.nextDouble() * Mth.TWO_PI;
                        double r = random.nextDouble() * radius * 0.8;
                        BlockPos ground = BlockPos.containing(sunCenter.x + Math.cos(angle) * r, sunCenter.y - 1, sunCenter.z + Math.sin(angle) * r);
                        BlockState state = world.getBlockState(ground);
                        for (int step = 0; step < 6 && state.isAir(); step++) {
                            ground = ground.below();
                            state = world.getBlockState(ground);
                        }
                        if (state.isAir()) {
                            continue;
                        }
                        double speed = 0.6 + random.nextDouble() * 1.2;
                        world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), ground.getX() + 0.5,
                                ground.getY() + 1.1, ground.getZ() + 0.5, Math.cos(angle) * speed, 0.4 + random.nextDouble() * 0.9,
                                Math.sin(angle) * speed);
                    }
                }
                if (ticks < 2) {
                    // sparks over the dome
                    for (int i = count(Math.min(260, 80 + radius * 6.0F) * detail, random); i > 0; i--) {
                        Vec3 out = scatter(random, 1.0).normalize();
                        out = new Vec3(out.x, Math.abs(out.y), out.z);
                        Vec3 velocity = out.scale(radius * (0.03 + 0.07 * random.nextDouble()));
                        world.addParticle(ParticlesRegistry.SUN_SPARK.get(), sunCenter.x, sunCenter.y + 0.5, sunCenter.z, velocity.x, velocity.y + 0.2, velocity.z);
                    }
                }
                if (ticks <= CruelSunEntity.BLAST_GROW) {
                    float front = CruelSunEntity.blastFront(radius, ticks);
                    for (int i = count(Math.min(70, 16 + radius * 3.0F) * heat * detail, random); i > 0; i--) {
                        Vec3 out = scatter(random, 1.0).normalize();
                        Vec3 point = sunCenter.add(out.scale(front * random.nextDouble()));
                        Vec3 velocity = out.scale(0.15 + random.nextDouble() * 0.35);
                        world.addParticle(random.nextBoolean() ? ParticlesRegistry.SUN_WISP.get() : ParticlesRegistry.SUN_SPARK.get(),
                                point.x, point.y, point.z, velocity.x, velocity.y, velocity.z);
                    }
                } else if (CruelSunEntity.blastFade(ticks) < 0.95F) {
                    for (int i = count((4 + radius * 0.4F) * detail, random); i > 0; i--) {
                        Vec3 point = sunCenter.add(scatter(random, radius * 0.8));
                        world.addParticle(random.nextInt(3) == 0 ? ParticleTypes.LARGE_SMOKE : ParticlesRegistry.SUN_WISP.get(),
                                point.x, point.y, point.z, 0, 0.04 + random.nextDouble() * 0.05, 0);
                    }
                    // gas off the ground
                    for (int i = count((3 + radius * 0.3F) * detail, random); i > 0; i--) {
                        double angle = random.nextDouble() * Mth.TWO_PI;
                        double r = radius * 0.85 * Math.sqrt(random.nextDouble());
                        world.addParticle(ParticlesRegistry.SUN_WISP.get(), sunCenter.x + Math.cos(angle) * r, sunCenter.y + 0.3 + random.nextDouble() * radius * 0.25,
                                sunCenter.z + Math.sin(angle) * r, 0, 0.15 + random.nextDouble() * 0.2, 0);
                    }
                    // falling embers
                    for (int i = count((2 + radius * 0.25F) * detail, random); i > 0; i--) {
                        Vec3 point = sunCenter.add((random.nextDouble() - 0.5) * radius * 1.6, radius * (0.4 + random.nextDouble() * 0.6),
                                (random.nextDouble() - 0.5) * radius * 1.6);
                        world.addParticle(ParticlesRegistry.SUN_SPARK.get(), point.x, point.y, point.z, (random.nextDouble() - 0.5) * 0.04,
                                -0.12 - random.nextDouble() * 0.1, (random.nextDouble() - 0.5) * 0.04);
                    }
                    // dust column
                    if (random.nextFloat() < 0.6F * detail) {
                        Vec3 point = sunCenter.add((random.nextDouble() - 0.5) * radius, -radius * 0.2, (random.nextDouble() - 0.5) * radius);
                        world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, point.x, point.y, point.z, 0, 0.08 + random.nextDouble() * 0.06, 0);
                    }
                }
            }
            default -> {
            }
        }
    }

    // surface glow and sparks
    private static void flicker(Level world, RandomSource random, Vec3 center, float orb, float amount) {
        for (int i = count(amount * 0.5F, random); i > 0; i--) {
            Vec3 out = scatter(random, 1.0).normalize();
            Vec3 point = center.add(out.scale(orb * 1.05));
            Vec3 velocity = out.scale(0.08 + random.nextDouble() * 0.12);
            world.addParticle(ParticlesRegistry.SUN_WISP.get(), point.x, point.y, point.z, velocity.x, velocity.y, velocity.z);
        }
        for (int i = count(amount * 0.4F, random); i > 0; i--) {
            Vec3 out = scatter(random, 1.0).normalize();
            Vec3 point = center.add(out.scale(orb));
            Vec3 velocity = out.scale(0.25 + random.nextDouble() * 0.45);
            world.addParticle(ParticlesRegistry.SUN_SPARK.get(), point.x, point.y, point.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private static Vec3 scatter(RandomSource random, double size) {
        return new Vec3((random.nextDouble() - 0.5) * 2 * size, (random.nextDouble() - 0.5) * 2 * size,
                (random.nextDouble() - 0.5) * 2 * size);
    }
}
