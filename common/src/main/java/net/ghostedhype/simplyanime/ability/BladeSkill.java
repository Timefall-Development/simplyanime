package net.ghostedhype.simplyanime.ability;

import net.ghostedhype.simplyanime.animation.AnimationClip;
import net.ghostedhype.simplyanime.animation.AnimationClips;
import net.ghostedhype.simplyanime.callout.Callout;

public enum BladeSkill {
    VORPAL_STRIKE("vorpal_strike", Callout.VORPAL_STRIKE),
    HORIZONTAL_SQUARE("horizontal_square", Callout.HORIZONTAL_SQUARE),
    STARBURST_STREAM("starburst_stream", Callout.STARBURST_STREAM);

    private final String clipName;
    public final Callout callout;

    BladeSkill(String clipName, Callout callout) {
        this.clipName = clipName;
        this.callout = callout;
    }

    public AnimationClip clip() {
        return AnimationClips.get(this.clipName);
    }

    // game tick a marker of the animation falls on
    public int tick(String marker) {
        AnimationClip clip = this.clip();
        return Math.round(clip.marker(marker) / clip.framesPerTick());
    }
}
