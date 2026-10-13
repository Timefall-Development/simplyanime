package net.hussain.simplyanime.platform;

import net.hussain.simplyanime.entity.DataDefiner;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class SyncedEntity extends Entity {

    protected SyncedEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected abstract void defineData(DataDefiner definer);

    @Override
    protected void defineSynchedData() {
        this.defineData(this.entityData::define);
    }
}
