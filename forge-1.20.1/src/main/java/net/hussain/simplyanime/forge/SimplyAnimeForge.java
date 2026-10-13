package net.hussain.simplyanime.forge;

import net.hussain.simplyanime.CommonEvents;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.SimplyAnime;
import net.hussain.simplyanime.ability.RhittaGrowth;
import net.hussain.simplyanime.command.SimplyAnimeCommand;
import net.hussain.simplyanime.registry.ItemsRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class SimplyAnimeForge {

    public SimplyAnimeForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SimplyAnime.init();
        CraftingHelper.register(new RecipeToggleSerializer());

        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(SimplyAnime::setup));
        modBus.addListener(SimplyAnimeForge::onCreativeTab);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(SimplyAnimeForge::onLevelTick);
        bus.addListener(SimplyAnimeForge::onLevelUnload);
        bus.addListener((ServerStoppedEvent event) -> CommonEvents.serverStopped());
        bus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> CommonEvents.playerGone(event.getEntity()));
        bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> CommonEvents.playerGone(event.getEntity()));
        bus.addListener(SimplyAnimeForge::onAttacked);
        bus.addListener(SimplyAnimeForge::onHurt);
        bus.addListener(SimplyAnimeForge::onAttack);
        bus.addListener(SimplyAnimeForge::onPlayerTick);
        bus.addListener(SimplyAnimeForge::onSize);
        bus.addListener((RegisterCommandsEvent event) -> SimplyAnimeCommand.register(event.getDispatcher()));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            SimplyAnimeForgeClient.init(modBus);
        }
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(Constants.SIMPLY_SWORDS_TAB)) {
            ItemsRegistry.all().forEach(item -> event.accept(item.get()));
        }
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            CommonEvents.levelTick(level);
        }
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CommonEvents.levelUnload(level);
        }
    }

    private static void onAttacked(LivingAttackEvent event) {
        if (CommonEvents.blocksDamage(event.getEntity(), event.getSource())) {
            event.setCanceled(true);
        }
    }

    private static void onHurt(LivingHurtEvent event) {
        event.setAmount(CommonEvents.scaleDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }

    private static void onAttack(AttackEntityEvent event) {
        if (CommonEvents.blocksAttack(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RhittaGrowth.tick(event.player);
        }
    }

    private static void onSize(EntityEvent.Size event) {
        float scale = RhittaGrowth.sizeScale(event.getEntity());
        if (scale != 1.0F) {
            event.setNewSize(event.getNewSize().scale(scale), false);
            event.setNewEyeHeight(event.getNewEyeHeight() * scale);
        }
    }
}
