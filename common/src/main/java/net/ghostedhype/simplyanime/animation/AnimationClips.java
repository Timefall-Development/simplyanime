package net.ghostedhype.simplyanime.animation;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationClips {

    private static final Map<String, AnimationClip> LOADED = new ConcurrentHashMap<>();

    public static AnimationClip get(String name) {
        return LOADED.computeIfAbsent(name, AnimationClips::load);
    }

    private static AnimationClip load(String name) {
        String path = "/data/simplyanime/animation/" + name + ".bin";
        try (InputStream raw = AnimationClips.class.getResourceAsStream(path)) {
            if (raw == null) {
                throw new IllegalStateException("missing animation " + path);
            }
            return new AnimationClip(name, new DataInputStream(new BufferedInputStream(raw)));
        } catch (IOException e) {
            throw new IllegalStateException("could not read animation " + path, e);
        }
    }
}
