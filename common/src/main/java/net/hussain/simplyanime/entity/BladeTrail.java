package net.hussain.simplyanime.entity;

import net.minecraft.world.phys.Vec3;

public class BladeTrail {

    private static final int SIZE = 96;

    private final Vec3[] base = new Vec3[SIZE];
    private final Vec3[] tip = new Vec3[SIZE];
    private final float[] time = new float[SIZE];
    private final float[] heat = new float[SIZE];
    private int next;
    private int count;

    public void add(Vec3 base, Vec3 tip, float time, float heat) {
        this.base[this.next] = base;
        this.tip[this.next] = tip;
        this.time[this.next] = time;
        this.heat[this.next] = heat;
        this.next = (this.next + 1) % SIZE;
        this.count = Math.min(SIZE, this.count + 1);
    }

    public int size() {
        return this.count;
    }

    private int slot(int index) {
        return (this.next - this.count + index + SIZE) % SIZE;
    }

    public Vec3 base(int index) {
        return this.base[this.slot(index)];
    }

    public Vec3 tip(int index) {
        return this.tip[this.slot(index)];
    }

    public float time(int index) {
        return this.time[this.slot(index)];
    }

    public float heat(int index) {
        return this.heat[this.slot(index)];
    }
}
