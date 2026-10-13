package net.ghostedhype.simplyanime;

import net.ghostedhype.simplyanime.ability.BladeSkillManager;
import net.ghostedhype.simplyanime.ability.EnumaElishAbilityManager;
import net.ghostedhype.simplyanime.ability.InvertedSpearAbilityManager;
import net.ghostedhype.simplyanime.ability.RhittaGrowth;
import net.ghostedhype.simplyanime.entity.CruelSunEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class CommonEvents {

    public static void levelTick(ServerLevel level) {
        InvertedSpearAbilityManager.tick(level);
        EnumaElishAbilityManager.tick(level);
        BladeSkillManager.tick(level);
    }

    public static void levelUnload(ServerLevel level) {
        InvertedSpearAbilityManager.clear(level);
        EnumaElishAbilityManager.clear(level);
        BladeSkillManager.clear(level);
    }

    public static void serverStopped() {
        InvertedSpearAbilityManager.clearAll();
        EnumaElishAbilityManager.clearAll();
        BladeSkillManager.clearAll();
        CruelSunEntity.clearAll();
    }

    // logged out or went to another dimension
    public static void playerGone(LivingEntity player) {
        InvertedSpearAbilityManager.clearActor(player);
        EnumaElishAbilityManager.clearActor(player);
        BladeSkillManager.clearActor(player);
    }

    // before armor, knockback or hurt time get touched
    public static boolean blocksDamage(LivingEntity entity, DamageSource source) {
        return EnumaElishAbilityManager.blocksDamage(entity, source) || CruelSunEntity.blocksDamage(entity, source);
    }

    public static boolean blocksAttack(LivingEntity attacker) {
        return EnumaElishAbilityManager.blocksAttack(attacker) || BladeSkillManager.blocksAttack(attacker);
    }

    public static float scaleDamage(LivingEntity target, DamageSource source, float amount) {
        return BladeSkillManager.scaleDamage(target, source, RhittaGrowth.scaleDamage(target, source, amount));
    }
}
