package net.hussain.simplyanime.client.renderer;

import net.hussain.simplyanime.config.ClientConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

// Iris and Oculus are optional, so their api is looked up by name. Both ship the same api package
public class ShaderCompat {

    private static final Object IRIS;
    private static final MethodHandle PACK_IN_USE;
    private static final MethodHandle SHADOW_PASS;

    static {
        Object iris = null;
        MethodHandle pack = null;
        MethodHandle shadow = null;
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            iris = lookup.findStatic(api, "getInstance", MethodType.methodType(api)).invoke();
            pack = lookup.findVirtual(api, "isShaderPackInUse", MethodType.methodType(boolean.class));
            shadow = lookup.findVirtual(api, "isRenderingShadowPass", MethodType.methodType(boolean.class));
        } catch (Throwable ignored) {
            iris = null;
        }
        IRIS = iris;
        PACK_IN_USE = pack;
        SHADOW_PASS = shadow;
    }

    public static boolean packInUse() {
        return call(PACK_IN_USE);
    }

    public static boolean shadowPass() {
        return call(SHADOW_PASS);
    }

    private static boolean call(MethodHandle method) {
        if (IRIS == null) {
            return false;
        }
        try {
            return (boolean) method.invoke(IRIS);
        } catch (Throwable ignored) {
            return false;
        }
    }

    // for the per pack tuning
    private static long nameRead;
    private static String name = "";

    public static boolean packNameHas(String part) {
        long now = System.currentTimeMillis();
        if (now - nameRead > 500) {
            nameRead = now;
            name = readName();
        }
        return name.contains(part);
    }

    // bsl, makeup and solas blow beacons out to white, solid parts the worst
    public static float gain(boolean solid) {
        float gain = 1.0F;
        if (packNameHas("bsl")) {
            gain = solid ? 0.22F : 0.4F;
        } else if (packNameHas("makeup")) {
            gain = solid ? 0.2F : 0.35F;
        } else if (packNameHas("solas")) {
            gain = solid ? 0.3F : 0.55F;
        } else if (packNameHas("photon")) {
            gain = solid ? 0.8F : 1.0F;
        }
        return gain * ClientConfig.get().shaderGlow;
    }

    private static String readName() {
        if (IRIS == null || !packInUse()) {
            return "";
        }
        for (String type : new String[]{"net.irisshaders.iris.Iris", "net.coderbot.iris.Iris"}) {
            try {
                Object value = Class.forName(type).getMethod("getCurrentPackName").invoke(null);
                return value == null ? "" : value.toString().toLowerCase();
            } catch (Throwable ignored) {
            }
        }
        return "";
    }
}
