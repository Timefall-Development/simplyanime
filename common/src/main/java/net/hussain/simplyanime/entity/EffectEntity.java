package net.hussain.simplyanime.entity;

import net.hussain.simplyanime.platform.SyncedEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// an entity that only exists to be drawn: never saved, never hit, no physics
public abstract class EffectEntity extends SyncedEntity {

    protected EffectEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    protected static Vector3f pack(Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    protected static Vec3 unpack(Vector3f vec) {
        return new Vec3(vec.x(), vec.y(), vec.z());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
