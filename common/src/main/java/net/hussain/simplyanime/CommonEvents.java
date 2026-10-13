package net.hussain.simplyanime;

import net.hussain.simplyanime.ability.EnumaElishAbilityManager;
import net.hussain.simplyanime.ability.InvertedSpearAbilityManager;
import net.hussain.simplyanime.ability.RhittaGrowth;
import net.hussain.simplyanime.entity.CruelSunEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// what the mod does on each game event. The loader listens for the event and calls in here
public class CommonEvents {

    public static void levelTick(ServerLevel level) {
        InvertedSpearAbilityManager.tick(level);
        EnumaElishAbilityManager.tick(level);
    }

    public static void levelUnload(ServerLevel level) {
        InvertedSpearAbilityManager.clear(level);
        EnumaElishAbilityManager.clear(level);
    }

    public static void serverStopped() {
        InvertedSpearAbilityManager.clearAll();
        EnumaElishAbilityManager.clearAll();
        CruelSunEntity.clearAll();
    }

    // logged out or went to another dimension
    public static void playerGone(LivingEntity player) {
        InvertedSpearAbilityManager.clearActor(player);
        EnumaElishAbilityManager.clearActor(player);
    }

    // before armor, knockback or hurt time get touched
    public static boolean blocksDamage(LivingEntity entity, DamageSource source) {
        return EnumaElishAbilityManager.blocksDamage(entity, source) || CruelSunEntity.blocksDamage(entity, source);
    }

    public static boolean blocksAttack(LivingEntity attacker) {
        return EnumaElishAbilityManager.blocksAttack(attacker);
    }

    public static float scaleDamage(LivingEntity target, DamageSource source, float amount) {
        return RhittaGrowth.scaleDamage(target, source, amount);
    }
}
