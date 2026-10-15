package net.ghostedhype.simplyanime.client.pose;

public class Ease {

    public static float out(float progress) {
        return 1.0F - (1.0F - progress) * (1.0F - progress);
    }

    public static float inOut(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }
}
