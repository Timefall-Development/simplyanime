package net.hussain.simplyanime.client.pose;

import net.hussain.simplyanime.animation.AnimationClip;
import net.hussain.simplyanime.animation.AnimationPose;
import net.hussain.simplyanime.animation.AttackStyle;

class BladePoseState {
    final AnimationPose pose = new AnimationPose();
    // the frame the pose was worked out for
    int tick = -1;
    float partialTick = -1.0F;
    final float[][] applied = new float[6][6];
    boolean hasApplied;
    boolean posed;
    AnimationClip attack;
    AttackStyle style;
    boolean attackMirror;
    float attackStart;
    int chain;
    int swingTick = -1;
    int lastSwingTime;
    boolean wasSwinging;
}
