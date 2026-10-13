package net.ghostedhype.simplyanime.neoforge;

import com.mojang.serialization.MapCodec;
import net.ghostedhype.simplyanime.CommonEvents;
import net.ghostedhype.simplyanime.Constants;
import net.ghostedhype.simplyanime.SimplyAnime;
import net.ghostedhype.simplyanime.ability.RhittaGrowth;
import net.ghostedhype.simplyanime.command.SimplyAnimeCommand;
import net.ghostedhype.simplyanime.registry.ItemsRegistry;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Constants.MOD_ID)
public class SimplyAnimeNeoForge {

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Constants.MOD_ID);

    public SimplyAnimeNeoForge(IEventBus modBus, Dist dist) {
        SimplyAnime.init();
        CONDITIONS.register("craftable", () -> RecipeToggleCondition.CODEC);
        CONDITIONS.register(modBus);

        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(SimplyAnime::setup));
        modBus.addListener(SimplyAnimeNeoForge::onCreativeTab);

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(SimplyAnimeNeoForge::onLevelTick);
        bus.addListener(SimplyAnimeNeoForge::onLevelUnload);
        bus.addListener((ServerStoppedEvent event) -> CommonEvents.serverStopped());
        bus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> CommonEvents.playerGone(event.getEntity()));
        bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> CommonEvents.playerGone(event.getEntity()));
        bus.addListener(SimplyAnimeNeoForge::onDamage);
        bus.addListener(SimplyAnimeNeoForge::onAttack);
        bus.addListener((PlayerTickEvent.Post event) -> RhittaGrowth.tick(event.getEntity()));
        bus.addListener(SimplyAnimeNeoForge::onSize);
        bus.addListener((RegisterCommandsEvent event) -> SimplyAnimeCommand.register(event.getDispatcher()));

        if (dist == Dist.CLIENT) {
            SimplyAnimeNeoForgeClient.init(modBus);
        }
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(Constants.SIMPLY_SWORDS_TAB)) {
            ItemsRegistry.all().forEach(item -> event.accept(item.get()));
        }
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CommonEvents.levelTick(level);
        }
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CommonEvents.levelUnload(level);
        }
    }

    private static void onDamage(LivingIncomingDamageEvent event) {
        if (CommonEvents.blocksDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(CommonEvents.scaleDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }

    private static void onAttack(AttackEntityEvent event) {
        if (CommonEvents.blocksAttack(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onSize(EntityEvent.Size event) {
        float scale = RhittaGrowth.sizeScale(event.getEntity());
        if (scale != 1.0F) {
            event.setNewSize(event.getNewSize().scale(scale));
        }
    }
}
