package net.hussain.simplyanime.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import org.jetbrains.annotations.Nullable;

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

    // the actor can be a summon, then the player behind it has a say too
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
}
