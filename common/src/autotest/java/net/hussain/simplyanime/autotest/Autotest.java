package net.hussain.simplyanime.autotest;

import com.mojang.logging.LogUtils;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

// flat test world, a husk casts each ability at a golem, screenshots go to <pack>_<effect>_<tick>.png, then it quits
public class Autotest {

    private static final Logger LOG = LogUtils.getLogger();
    private static final int GROUND = -60;
    private static final String SHADERS = System.getProperty("simplyanime.autotest.shaders", "");
    private static final String ONLY = System.getProperty("simplyanime.autotest.effect", "");
    private static final String TIME = System.getProperty("simplyanime.autotest.time", "13200");

    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static int ticks;
    private static int wait;
    private static boolean worldRequested;
    private static boolean scripted;

    private record Step(int delay, Runnable action) {
    }

    public static void tick(Minecraft mc) {
        ticks++;
        if (!worldRequested) {
            if (ticks > 60 && mc.level == null) {
                worldRequested = true;
                mc.options.pauseOnLostFocus = false;
                mc.options.cloudStatus().set(CloudStatus.OFF);
                mc.options.gamma().set(0.5);
                mc.options.renderDistance().set(12);
                mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
                mc.options.onboardAccessibility = false;
                mc.options.save();
                createWorld(mc);
            }
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!scripted) {
            scripted = true;
            script(mc);
            wait = 100;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.poll();
        if (step == null) {
            return;
        }
        step.action().run();
        Step next = STEPS.peek();
        wait = next == null ? 0 : next.delay();
    }

    private static void createWorld(Minecraft mc) {
        LOG.info("[autotest] creating world");
        LevelSettings info = new LevelSettings("autotest", GameType.CREATIVE, false, Difficulty.NORMAL, true,
                new GameRules(), WorldDataConfiguration.DEFAULT);
        AutotestMod.createWorld(mc, info, new WorldOptions(4317L, false, false),
                registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
    }

    private static void cmd(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // stay in the air, otherwise the camera drops after every tp
            mc.player.getAbilities().flying = true;
            mc.player.onUpdateAbilities();
            mc.player.connection.sendCommand(command);
        }
    }

    private static void add(int delay, Runnable action) {
        STEPS.add(new Step(delay, action));
    }

    private static void script(Minecraft mc) {
        add(0, () -> {
            cmd("gamerule doDaylightCycle false");
            cmd("gamerule doMobSpawning false");
            cmd("gamerule doWeatherCycle false");
            cmd("gamerule mobGriefing false");
            cmd("gamerule sendCommandFeedback false");
            cmd("time set " + TIME);
            cmd("weather clear");
            mc.options.hideGui = true;
        });
        List<String> packs = new ArrayList<>();
        packs.add("none");
        File[] files = new File(mc.gameDirectory, "shaderpacks").listFiles((d, n) -> n.endsWith(".zip"));
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                if (wantedPack(f.getName())) {
                    packs.add(f.getName());
                }
            }
        }
        add(20, () -> cmd("satest check"));
        if (wanted("fp")) {
            firstPerson(mc, "ea", "enuma_elish", new int[]{20, 60, 82, 100, 190});
            firstPerson(mc, "sun", "divine_axe_rhitta", new int[]{30, 100, 124, 150});
        }
        for (String pack : packs) {
            String tag = pack.replace(".zip", "").replaceAll("[^A-Za-z0-9]", "");
            add(10, () -> setPack(pack));
            // shader compile time
            add(pack.equals("none") ? 40 : 200, () -> LOG.info("[autotest] pack ready: {}", pack));
            if (wanted("sun")) {
                sun(mc, tag);
            }
            if (wanted("ea")) {
                ea(mc, tag);
            }
            if (wanted("field")) {
                spear(mc, tag, "field", 4, new int[]{4, 14, 28, 46, 57, 62, 68}, "tp @s 7 " + (GROUND + 4) + " 6 facing 0 " + (GROUND + 1) + " -2");
            }
            if (wanted("strike")) {
                spear(mc, tag, "strike", 9, new int[]{4, 8, 11, 14, 18, 23}, "tp @s 9 " + (GROUND + 4) + " 3 facing 0 " + (GROUND + 1) + " -5");
            }
        }
        add(20, () -> {
            setPack("none");
            LOG.info("[autotest] done");
            mc.stop();
        });
    }

    // "all" or parts of pack names, comma separated
    private static boolean wantedPack(String file) {
        if (SHADERS.equals("all")) {
            return true;
        }
        for (String part : SHADERS.toLowerCase().split(",")) {
            if (!part.isBlank() && file.toLowerCase().contains(part.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean wanted(String effect) {
        return ONLY.isEmpty() || ONLY.contains(effect);
    }

    private static void scene(String weapon, int distance, String camera) {
        add(5, () -> {
            cmd("kill @e[type=!player]");
            cmd("summon minecraft:iron_golem 0 " + GROUND + " -" + distance + " {NoAI:1b,PersistenceRequired:1b,Health:100f}");
            cmd("summon minecraft:husk 0 " + GROUND + " 0 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
            cmd("item replace entity @e[type=minecraft:husk,limit=1] weapon.mainhand with simplyanime:" + weapon);
            cmd(camera);
        });
        add(20, () -> {
            cmd("kill @e[type=item]");
            cmd("effect give @e[type=minecraft:iron_golem] minecraft:resistance infinite 4 true");
        });
    }

    // a command that leaves the player standing on the ground
    private static void plain(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand(command);
        }
    }

    // the player casts it themself with the HUD on, for the hand pose and the ability name
    private static void firstPerson(Minecraft mc, String effect, String weapon, int[] at) {
        add(5, () -> {
            plain("kill @e[type=!player]");
            plain("summon minecraft:iron_golem 0 " + GROUND + " -14 {NoAI:1b,PersistenceRequired:1b,Health:100f}");
            mc.player.getAbilities().flying = false;
            mc.player.onUpdateAbilities();
            plain("tp @s 0 " + GROUND + " 0 180 -12");
            plain("item replace entity @s weapon.mainhand with simplyanime:" + weapon);
            mc.options.hideGui = false;
        });
        add(30, () -> {
            plain("effect give @e[type=minecraft:iron_golem] minecraft:resistance infinite 4 true");
            plain("satest " + effect);
        });
        shots(mc, "none_fp", effect, at);
        add(40, () -> {
            plain("item replace entity @s weapon.mainhand with minecraft:air");
            plain("simplyanime cooldown reset");
            mc.options.hideGui = true;
        });
    }

    private static void shots(Minecraft mc, String tag, String effect, int[] at) {
        int last = 0;
        for (int t : at) {
            String name = tag + "_" + effect + "_" + String.format("%03d", t) + ".png";
            add(t - last, () -> Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> {
            }));
            last = t;
        }
        add(40, () -> {
        });
    }

    private static void sun(Minecraft mc, String tag) {
        scene("divine_axe_rhitta", 34, "tp @s 46 " + (GROUND + 12) + " -14 facing 0 " + (GROUND + 9) + " -14");
        add(2, () -> cmd("execute as @e[type=minecraft:husk,limit=1] run satest sun"));
        shots(mc, tag, "sun", new int[]{12, 24, 40, 70, 116, 128, 146, 154, 166, 184, 204});
    }

    private static void ea(Minecraft mc, String tag) {
        scene("enuma_elish", 40, "tp @s 30 " + (GROUND + 9) + " 6 facing 0 " + (GROUND + 4) + " -16");
        add(2, () -> cmd("execute as @e[type=minecraft:husk,limit=1] run satest ea"));
        shots(mc, tag, "ea", new int[]{30, 70, 86, 92, 120, 170, 203, 212});
    }

    private static void spear(Minecraft mc, String tag, String effect, int distance, int[] at, String camera) {
        scene("inverted_spear_of_heaven", distance, camera);
        add(2, () -> cmd("execute as @e[type=minecraft:husk,limit=1] run satest " + effect));
        shots(mc, tag, effect, at);
    }

    private static void setPack(String pack) {
        for (String type : new String[]{"net.irisshaders.iris.Iris", "net.coderbot.iris.Iris"}) {
            try {
                Class<?> iris = Class.forName(type);
                Object config = iris.getMethod("getIrisConfig").invoke(null);
                boolean on = !pack.equals("none");
                if (on) {
                    config.getClass().getMethod("setShaderPackName", String.class).invoke(config, pack);
                }
                config.getClass().getMethod("setShadersEnabled", boolean.class).invoke(config, on);
                config.getClass().getMethod("save").invoke(config);
                iris.getMethod("reload").invoke(null);
                LOG.info("[autotest] switched shaders to {}", pack);
                return;
            } catch (ClassNotFoundException e) {
                continue;
            } catch (Throwable e) {
                LOG.error("[autotest] could not switch shader pack to {}: {}", pack, e.toString());
                return;
            }
        }
    }
}
