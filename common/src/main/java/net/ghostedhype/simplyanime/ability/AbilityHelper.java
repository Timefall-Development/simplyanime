package net.ghostedhype.simplyanime.ability;

import net.ghostedhype.simplyanime.platform.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.api.SpellScalingProfile;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

// bits every ability needs
public class AbilityHelper {

    public static void playSound(ServerLevel world, Vec3 at, SoundEvent sound, float volume, float pitch) {
        if (volume > 0) {
            world.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    public static double distanceToPoint(AABB box, Vec3 point) {
        double dx = Math.max(Math.max(box.minX - point.x, 0.0), point.x - box.maxX);
        double dy = Math.max(Math.max(box.minY - point.y, 0.0), point.y - box.maxY);
        double dz = Math.max(Math.max(box.minZ - point.z, 0.0), point.z - box.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static boolean canHit(ServerLevel world, LivingEntity target, LivingEntity actor, @Nullable UUID ownerId) {
        if (target == actor || !target.isAlive() || !SimplySwordsAPI.isValidAbilityTarget(target, actor)) {
            return false;
        }
        return canHitOwner(world, target, ownerId);
    }

    public static boolean canHitOwner(ServerLevel world, LivingEntity target, @Nullable UUID ownerId) {
        if (ownerId == null) {
            return true;
        }
        Entity owner = world.getEntity(ownerId);
        return !(owner instanceof LivingEntity livingOwner)
                || target != livingOwner && SimplySwordsAPI.isValidAbilityTarget(target, livingOwner);
    }

    @Nullable
    public static Vec3 strike(ServerLevel world, LivingEntity actor, @Nullable UUID ownerId, ItemStack stack,
                              Entity entity, Entity root, float damage) {
        if (root instanceof EnderDragon dragon) {
            if (dragon.isAlive() && canHitOwner(world, dragon, ownerId)) {
                dragon.hurt(dragon.head, SimplySwordsAPI.getWeaponDamageSource(actor), damage);
                return dragon.head.position();
            }
            return null;
        }
        if (root instanceof LivingEntity target) {
            if (canHit(world, target, actor, ownerId) && SimplySwordsAPI.applyAbilityMagicDamageThroughIframes(world, actor,
                    stack, target, damage, SpellScalingProfile.ARCANE)) {
                return target.position().add(0, target.getBbHeight() * 0.5, 0);
            }
            return null;
        }
        if (Platform.isPart(entity)) {
            entity.hurt(SimplySwordsAPI.getWeaponDamageSource(actor), damage);
        }
        return null;
    }

    public static Vec3 look(LivingEntity entity) {
        return Vec3.directionFromRotation(entity.getXRot(), entity.getYHeadRot()).normalize();
    }

    public static Vec3 toWorld(Vector3f local, Vec3 origin, float yaw, boolean mirror) {
        float angle = (180.0F - yaw) * Mth.DEG_TO_RAD;
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        float x = mirror ? -local.x : local.x;
        return origin.add(x * cos + local.z * sin, local.y, -x * sin + local.z * cos);
    }
}
