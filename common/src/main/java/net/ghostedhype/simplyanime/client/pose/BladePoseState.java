package net.ghostedhype.simplyanime.client.pose;

import net.ghostedhype.simplyanime.animation.AnimationClip;
import net.ghostedhype.simplyanime.animation.AnimationPose;
import net.ghostedhype.simplyanime.animation.AttackStyle;

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
