package net.ghostedhype.simplyanime.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;

public class AnimationPose {

    public final Vector3f[] position = new Vector3f[AnimationClip.PARTS];
    public final Quaternionf[] rotation = new Quaternionf[AnimationClip.PARTS];
    public final float[] weight = new float[AnimationClip.PARTS];
    public float locked;
    public int rightSword;
    public int leftSword;

    private final Vector3f scratchPosition = new Vector3f();
    private final Quaternionf scratchRotation = new Quaternionf();

    public AnimationPose() {
        for (int i = 0; i < AnimationClip.PARTS; i++) {
            this.position[i] = new Vector3f();
            this.rotation[i] = new Quaternionf();
        }
    }

    public void clear() {
        Arrays.fill(this.weight, 0.0F);
        this.locked = 0.0F;
        this.rightSword = 0;
        this.leftSword = 0;
    }

    public boolean isEmpty() {
        for (float w : this.weight) {
            if (w > 0.0F) {
                return false;
            }
        }
        return true;
    }

    // lays a clip over what is already there
    public void add(AnimationClip clip, float frame, float amount) {
        if (amount <= 0.0F) {
            return;
        }
        for (int part = 0; part < AnimationClip.PARTS; part++) {
            if (!clip.sample(part, frame, this.scratchPosition, this.scratchRotation)) {
                this.weight[part] *= 1.0F - amount;
                continue;
            }
            if (this.weight[part] <= 0.0F) {
                this.position[part].set(this.scratchPosition);
                this.rotation[part].set(this.scratchRotation);
                this.weight[part] = amount;
            } else {
                this.position[part].lerp(this.scratchPosition, amount);
                this.rotation[part].nlerp(this.scratchRotation, amount);
                this.weight[part] += (1.0F - this.weight[part]) * amount;
            }
        }
        if (amount >= 0.5F) {
            this.rightSword = clip.rightSword;
            this.leftSword = clip.leftSword;
        }
    }

    // for a left handed wielder: everything swaps sides
    public void mirror() {
        this.swap(AnimationClip.RIGHT_ARM, AnimationClip.LEFT_ARM);
        this.swap(AnimationClip.RIGHT_LEG, AnimationClip.LEFT_LEG);
        this.swap(AnimationClip.RIGHT_ITEM, AnimationClip.LEFT_ITEM);
        for (int part = 0; part < AnimationClip.PARTS; part++) {
            this.position[part].x = -this.position[part].x;
            Quaternionf q = this.rotation[part];
            q.set(q.x, -q.y, -q.z, q.w);
        }
        int sword = this.rightSword;
        this.rightSword = this.leftSword;
        this.leftSword = sword;
    }

    private void swap(int a, int b) {
        this.scratchPosition.set(this.position[a]);
        this.position[a].set(this.position[b]);
        this.position[b].set(this.scratchPosition);
        this.scratchRotation.set(this.rotation[a]);
        this.rotation[a].set(this.rotation[b]);
        this.rotation[b].set(this.scratchRotation);
        float w = this.weight[a];
        this.weight[a] = this.weight[b];
        this.weight[b] = w;
    }
}
