package net.ghostedhype.simplyanime.registry;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.util.platform.Registrar;
import me.fzzyhmstrs.fzzy_config.util.platform.RegistrySupplier;
import net.ghostedhype.simplyanime.Constants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class ParticlesRegistry {

    private static final Registrar<ParticleType<?>> PARTICLES =
            ConfigApiJava.platform().createRegistrar(Constants.MOD_ID, BuiltInRegistries.PARTICLE_TYPE);

    public static final Supplier<SimpleParticleType> HEAVEN_SPARK = register("heaven_spark");
    public static final Supplier<SimpleParticleType> SEVERED_ASH = register("severed_ash");
    public static final Supplier<SimpleParticleType> RUPTURE_EMBER = register("rupture_ember");
    public static final Supplier<SimpleParticleType> SUN_SPARK = register("sun_spark");
    public static final Supplier<SimpleParticleType> SUN_WISP = register("sun_wisp");
    public static final Supplier<SimpleParticleType> STAR_SPARK = register("star_spark");
    public static final Supplier<SimpleParticleType> CRIMSON_SPARK = register("crimson_spark");
    public static final Supplier<SimpleParticleType> FROST_SHARD = register("frost_shard");

    public static void init() {
        PARTICLES.init();
    }

    private static Supplier<SimpleParticleType> register(String name) {
        // the constructor is protected
        RegistrySupplier<ParticleType<?>> entry = PARTICLES.register(name, () -> new SimpleParticleType(true) {});
        return () -> (SimpleParticleType) entry.get();
    }
}
