package net.ghostedhype.simplyanime.entity;

import net.ghostedhype.simplyanime.platform.SyncedEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public abstract class EffectEntity extends SyncedEntity {

    protected EffectEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    protected static Vector3f pack(Vec3 vector) {
        return new Vector3f((float) vector.x, (float) vector.y, (float) vector.z);
    }

    protected static Vec3 unpack(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
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
