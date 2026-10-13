package net.hussain.simplyanime.entity;

import net.minecraft.network.syncher.EntityDataAccessor;

// how synched data gets its default changed between versions, SyncedEntity hides that behind this
public interface DataDefiner {

    <T> void define(EntityDataAccessor<T> key, T value);
}
