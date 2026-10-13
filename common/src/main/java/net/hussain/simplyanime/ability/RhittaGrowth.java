package net.hussain.simplyanime.ability;

import net.hussain.simplyanime.config.RhittaConfig;
import net.hussain.simplyanime.entity.CruelSunEntity;
import net.hussain.simplyanime.item.DivineAxeRhittaItem;
import net.hussain.simplyanime.platform.Platform;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

// passive side: sun bonus on basic hits and the size change
public class RhittaGrowth {

    // prev, current, the scale the hitbox was last built with
    private static final Map<Player, float[]> SERVER = new WeakHashMap<>();
    private static final Map<Player, float[]> CLIENT = new WeakHashMap<>();

    public static boolean holding(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof DivineAxeRhittaItem;
    }

    // what a hit comes to after the sun has had its say. Also sets the target alight
    public static float scaleDamage(LivingEntity target, DamageSource source, float amount) {
        if (CruelSunEntity.dealing || !(source.getEntity() instanceof LivingEntity attacker)
                || source.getDirectEntity() != attacker || !holding(attacker)) {
            return amount;
        }
        // plain melee only, gem and runic procs keep their own numbers
        if (!source.is(DamageTypes.PLAYER_ATTACK) && !source.is(DamageTypes.MOB_ATTACK)
                && !source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) {
            return amount;
        }
        float power = SunPower.sunPower(attacker.level(), attacker);
        RhittaConfig.AxeSettings cfg = RhittaConfig.get().axe;
        float seconds = cfg.burnSeconds + cfg.burnSunScaling * Math.max(0.0F, power - 1.0F);
        if (seconds > 0) {
            Platform.ignite(target, Math.round(seconds));
        }
        return amount * power;
    }

    public static float targetScale(Player player) {
        RhittaConfig.GrowthSettings cfg = RhittaConfig.get().growth;
        if (!cfg.enabled || !holding(player)) {
            return 1.0F;
        }
        float sun = SunPower.fraction(SunPower.sunPower(player.level(), player));
        return Mth.lerp(sun, 1.0F, Math.max(1.0F, cfg.maxScale));
    }

    public static float renderScale(Player player, float partialTick) {
        float[] s = CLIENT.get(player);
        return s == null ? 1.0F : Mth.lerp(partialTick, s[0], s[1]);
    }

    public static void tick(Player player) {
        Map<Player, float[]> map = player.level().isClientSide() ? CLIENT : SERVER;
        float[] s = map.computeIfAbsent(player, p -> new float[]{1.0F, 1.0F, 1.0F});
        s[0] = s[1];
        float target = targetScale(player);
        float next = s[1] + (target - s[1]) * 0.08F;
        if (Math.abs(target - next) < 0.002F) {
            next = target;
        }

        boolean hitbox = RhittaConfig.get().growth.scaleHitbox;
        // never grow into a ceiling or a wall
        if (hitbox && next > s[1] && !roomFor(player, next)) {
            next = s[1];
        }
        s[1] = next;

        float built = hitbox ? next : 1.0F;
        if (Math.abs(built - s[2]) > 0.01F || (built == target && built != s[2])) {
            s[2] = built;
            player.refreshDimensions();
            if (!player.level().isClientSide()) {
                Platform.setReach(player, built);
            }
        }
    }

    private static boolean roomFor(Player player, float scale) {
        EntityDimensions size = player.getDimensions(player.getPose()).scale(scale);
        return player.level().noCollision(player, size.makeBoundingBox(player.position()).deflate(1.0E-7));
    }

    // how much bigger the hitbox and eye height should be built, 1 for anyone not grown
    public static float sizeScale(Entity entity) {
        if (!(entity instanceof Player player) || player.level() == null) {
            return 1.0F;
        }
        float[] s = (player.level().isClientSide() ? CLIENT : SERVER).get(player);
        return s == null ? 1.0F : s[2];
    }
}
