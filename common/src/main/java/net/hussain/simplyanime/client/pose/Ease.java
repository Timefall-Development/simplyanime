package net.hussain.simplyanime.client.pose;

public class Ease {

    public static float out(float t) {
        return 1.0F - (1.0F - t) * (1.0F - t);
    }

    public static float inOut(float t) {
        return t * t * (3.0F - 2.0F * t);
    }
}
