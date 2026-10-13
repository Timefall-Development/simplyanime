package net.hussain.simplyanime.autotest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Function;

// dev only, runClient -Pautotest
@Mod("simplyanime_autotest")
public class AutotestMod {

    public AutotestMod() {
        if (!Boolean.getBoolean("simplyanime.autotest")) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> CastCommand.register(event.getDispatcher()));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
                if (event.phase == TickEvent.Phase.END) {
                    Autotest.tick(Minecraft.getInstance());
                }
            });
        }
    }

    // the world creation call isn't the same on every version
    static void createWorld(Minecraft mc, LevelSettings settings, WorldOptions options,
                            Function<RegistryAccess, WorldDimensions> dimensions) {
        mc.createWorldOpenFlows().createFreshLevel("autotest", settings, options, dimensions);
    }
}
