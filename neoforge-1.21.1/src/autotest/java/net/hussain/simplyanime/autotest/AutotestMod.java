package net.hussain.simplyanime.autotest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.function.Function;

// dev only, runClient -Pautotest
@Mod("simplyanime_autotest")
public class AutotestMod {

    public AutotestMod(Dist dist) {
        if (!Boolean.getBoolean("simplyanime.autotest")) {
            return;
        }
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> CastCommand.register(event.getDispatcher()));
        if (dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> Autotest.tick(Minecraft.getInstance()));
        }
    }

    // the world creation call isn't the same on every version
    static void createWorld(Minecraft mc, LevelSettings settings, WorldOptions options,
                            Function<RegistryAccess, WorldDimensions> dimensions) {
        mc.createWorldOpenFlows().createFreshLevel("autotest", settings, options, dimensions, null);
    }
}
