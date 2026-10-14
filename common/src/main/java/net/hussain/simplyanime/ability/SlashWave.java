package net.hussain.simplyanime.ability;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SlashWave {

    public final Vec3 center;
    public final Vec3 axis;
    public final Vec3 direction;
    public final float half;
    public final float speed;
    public final float range;
    public final float spread;
    public final float thickness;
    public float age;

    // server side
    public float damage;
    public double knockback;
    public Set<UUID> struck = new HashSet<>();

    public SlashWave(Vec3 center, Vec3 axis, Vec3 direction, float half, float speed, float range, float spread,
                     float thickness) {
        this.center = center;
        this.axis = axis;
        this.direction = direction;
        this.half = half;
        this.speed = speed;
        this.range = range;
        this.spread = spread;
        this.thickness = thickness;
    }

    public static Vec3 perpendicular(Vec3 direction) {
        Vec3 helper = Math.abs(direction.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        return direction.cross(helper).normalize();
    }

    public float travelled(float age) {
        return Math.min(this.range, this.speed * age);
    }

    public Vec3 centerAt(float age) {
        return this.center.add(this.direction.scale(this.travelled(age)));
    }

    public float halfAt(float age) {
        return this.half * (1.0F + this.spread * this.travelled(age));
    }

    public boolean spent(float age) {
        return this.speed * age > this.range;
    }

    public boolean touches(AABB box, float age) {
        Vec3 middle = this.centerAt(age);
        float half = this.halfAt(age);
        int steps = Math.max(2, (int) Math.ceil(half * 2.0F / 0.75F));
        for (int i = 0; i <= steps; i++) {
            double along = -half + 2.0 * half * i / steps;
            if (AbilityHelper.distanceToPoint(box, middle.add(this.axis.scale(along))) <= this.thickness) {
                return true;
            }
        }
        return false;
    }
}
