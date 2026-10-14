package net.hussain.simplyanime.entity;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface DataDefiner {

    <T> void define(EntityDataAccessor<T> key, T value);
}
