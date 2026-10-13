package net.ghostedhype.simplyanime.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AnimationClip {

    public static final int ROOT = 0;
    public static final int HEAD = 1;
    public static final int BODY = 2;
    public static final int RIGHT_ARM = 3;
    public static final int LEFT_ARM = 4;
    public static final int RIGHT_LEG = 5;
    public static final int LEFT_LEG = 6;
    public static final int RIGHT_ITEM = 7;
    public static final int LEFT_ITEM = 8;
    public static final int PARTS = 9;

    public static final int RIGHT_TIP = 0;
    public static final int RIGHT_BASE = 1;

    public static final int ELUCIDATOR = 1;
    public static final int DARK_REPULSER = 2;

    public static final float PIXEL = 0.9375F / 16.0F;
    private static final int MAGIC = 0x5341414E;

    public record Marker(String name, int frame) {
    }

    public final String name;
    public final int fps;
    public final int frames;
    public final boolean loop;
    // which sword sits in each hand, 0 for an empty one
    public final int rightSword;
    public final int leftSword;
    private final int[] slot = new int[PARTS];
    private final int stride;
    private final float[] transforms;
    private final float[] points;
    private final List<Marker> markers = new ArrayList<>();

    AnimationClip(String name, DataInputStream in) throws IOException {
        this.name = name;
        if (in.readInt() != MAGIC || in.readUnsignedByte() != 3) {
            throw new IOException("not an animation clip: " + name);
        }
        this.fps = in.readUnsignedByte();
        this.frames = in.readUnsignedShort();
        this.loop = in.readUnsignedByte() != 0;
        int mask = in.readUnsignedShort();
        this.rightSword = in.readUnsignedByte();
        this.leftSword = in.readUnsignedByte();
        for (int count = in.readUnsignedShort(); count > 0; count--) {
            byte[] raw = new byte[in.readUnsignedShort()];
            in.readFully(raw);
            this.markers.add(new Marker(new String(raw, StandardCharsets.UTF_8), in.readUnsignedShort()));
        }
        // the root is written first, then the parts in order
        int next = 0;
        this.slot[ROOT] = (mask & 0x100) != 0 ? next++ : -1;
        for (int part = HEAD; part < PARTS; part++) {
            this.slot[part] = (mask & 1 << part - 1) != 0 ? next++ : -1;
        }
        this.stride = next * 7;
        this.transforms = new float[this.frames * this.stride];
        this.points = new float[this.frames * 12];
        for (int frame = 0; frame < this.frames; frame++) {
            for (int i = 0; i < this.stride; i++) {
                this.transforms[frame * this.stride + i] = in.readFloat();
            }
            for (int i = 0; i < 12; i++) {
                this.points[frame * 12 + i] = in.readFloat() * PIXEL;
            }
        }
    }

    public float framesPerTick() {
        return this.fps / 20.0F;
    }

    public int lengthTicks() {
        return Math.round((this.frames - 1) / this.framesPerTick());
    }

    public List<Marker> markers() {
        return this.markers;
    }

    public int marker(String name) {
        for (Marker marker : this.markers) {
            if (marker.name().equals(name)) {
                return marker.frame();
            }
        }
        return -1;
    }

    private float wrap(float frame) {
        int last = this.frames - 1;
        if (this.loop) {
            frame %= last;
            return frame < 0 ? frame + last : frame;
        }
        return Math.max(0.0F, Math.min(last, frame));
    }

    public boolean sample(int part, float frame, Vector3f position, Quaternionf rotation) {
        return this.slot[part] >= 0 && this.read(this.slot[part] * 7, frame, position, rotation);
    }

    private boolean read(int at, float frame, Vector3f position, Quaternionf rotation) {
        frame = this.wrap(frame);
        int from = (int) frame;
        int to = Math.min(this.frames - 1, from + 1);
        float t = frame - from;
        int a = from * this.stride + at;
        int b = to * this.stride + at;
        float[] d = this.transforms;
        position.set(d[a] + (d[b] - d[a]) * t, d[a + 1] + (d[b + 1] - d[a + 1]) * t, d[a + 2] + (d[b + 2] - d[a + 2]) * t);
        float dot = d[a + 3] * d[b + 3] + d[a + 4] * d[b + 4] + d[a + 5] * d[b + 5] + d[a + 6] * d[b + 6];
        float s = dot < 0 ? -t : t;
        rotation.set(d[a + 3] * (1 - t) + d[b + 3] * s, d[a + 4] * (1 - t) + d[b + 4] * s,
                d[a + 5] * (1 - t) + d[b + 5] * s, d[a + 6] * (1 - t) + d[b + 6] * s).normalize();
        return true;
    }

    public Vector3f point(int which, float frame, Vector3f out) {
        frame = this.wrap(frame);
        int from = (int) frame;
        int to = Math.min(this.frames - 1, from + 1);
        float t = frame - from;
        int a = from * 12 + which * 3;
        int b = to * 12 + which * 3;
        float[] d = this.points;
        return out.set(d[a] + (d[b] - d[a]) * t, d[a + 1] + (d[b + 1] - d[a + 1]) * t, d[a + 2] + (d[b + 2] - d[a + 2]) * t);
    }
}
