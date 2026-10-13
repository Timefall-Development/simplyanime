package net.hussain.simplyanime.registry;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.util.platform.Registrar;
import me.fzzyhmstrs.fzzy_config.util.platform.RegistrySupplier;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.entity.CruelSunEntity;
import net.hussain.simplyanime.entity.EnumaElishVisualEntity;
import net.hussain.simplyanime.entity.HeavenChainVisualEntity;
import net.hussain.simplyanime.entity.HeavenMarkVisualEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class EntityRegistry {

    private static final Registrar<EntityType<?>> ENTITIES =
            ConfigApiJava.platform().createRegistrar(Constants.MOD_ID, BuiltInRegistries.ENTITY_TYPE);

    public static final Supplier<EntityType<HeavenChainVisualEntity>> HEAVEN_CHAIN_VISUAL =
            register("heaven_chain_visual", HeavenChainVisualEntity::new, 0.35F, 96);
    public static final Supplier<EntityType<HeavenMarkVisualEntity>> HEAVEN_MARK_VISUAL =
            register("heaven_mark_visual", HeavenMarkVisualEntity::new, 0.35F, 96);
    public static final Supplier<EntityType<EnumaElishVisualEntity>> ENUMA_ELISH_VISUAL =
            register("enuma_elish_visual", EnumaElishVisualEntity::new, 0.35F, 16);
    public static final Supplier<EntityType<CruelSunEntity>> CRUEL_SUN =
            register("cruel_sun", CruelSunEntity::new, 0.5F, 12);

    public static void init() {
        ENTITIES.init();
    }

    // all of them are effect entities: tiny, tracked every tick, range in chunks
    @SuppressWarnings("unchecked")
    private static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.EntityFactory<T> factory,
                                                                       float size, int range) {
        RegistrySupplier<EntityType<?>> entry = ENTITIES.register(name, () ->
                EntityType.Builder.of(factory, MobCategory.MISC)
                        .sized(size, size)
                        .clientTrackingRange(range)
                        .updateInterval(1)
                        .build(Constants.id(name).toString()));
        return () -> (EntityType<T>) entry.get();
    }
}
