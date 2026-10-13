package net.ghostedhype.simplyanime;

import net.ghostedhype.simplyanime.callout.CalloutPayload;
import net.ghostedhype.simplyanime.config.Config;
import net.ghostedhype.simplyanime.config.RhittaConfig;
import net.ghostedhype.simplyanime.registry.EntityRegistry;
import net.ghostedhype.simplyanime.registry.ItemsRegistry;
import net.ghostedhype.simplyanime.registry.ParticlesRegistry;
import net.ghostedhype.simplyanime.registry.SoundRegistry;
import net.minecraft.world.level.block.Blocks;
import net.sweenus.simplyswords.api.SimplySwordsAPI;

public class SimplyAnime {

    public static ClientHooks client = ClientHooks.NONE;

    public static void init() {
        Config.init();
        RhittaConfig.init();
        ItemsRegistry.init();
        SoundRegistry.init();
        EntityRegistry.init();
        ParticlesRegistry.init();
        CalloutPayload.register();
    }

    // once everything is registered
    public static void setup() {
        SimplySwordsAPI.registerUniqueLoot(ItemsRegistry.INVERTED_SPEAR_OF_HEAVEN.get(), 1);
        SimplySwordsAPI.registerWeaponType(ItemsRegistry.INVERTED_SPEAR_OF_HEAVEN.get(), Constants.location("simplyswords", "sai"));
        SimplySwordsAPI.registerTransformation(Blocks.CHAIN, Constants.id("inverted_spear_of_heaven"));

        // also craftable, see the enuma_elish recipe
        if (Config.weapons.enuma_elish.lootable) {
            SimplySwordsAPI.registerUniqueLoot(ItemsRegistry.ENUMA_ELISH.get(), 1);
        }
        SimplySwordsAPI.registerWeaponType(ItemsRegistry.ENUMA_ELISH.get(), Constants.location("simplyswords", "claymore"));

        if (RhittaConfig.get().axe.lootable) {
            SimplySwordsAPI.registerUniqueLoot(ItemsRegistry.DIVINE_AXE_RHITTA.get(), 1);
        }
        SimplySwordsAPI.registerWeaponType(ItemsRegistry.DIVINE_AXE_RHITTA.get(), Constants.location("simplyswords", "greataxe"));

        if (Config.weapons.elucidator.lootable) {
            SimplySwordsAPI.registerUniqueLoot(ItemsRegistry.ELUCIDATOR.get(), 1);
        }
        SimplySwordsAPI.registerWeaponType(ItemsRegistry.ELUCIDATOR.get(), Constants.location("simplyswords", "longsword"));

        if (Config.weapons.dark_repulser.lootable) {
            SimplySwordsAPI.registerUniqueLoot(ItemsRegistry.DARK_REPULSER.get(), 1);
        }
        SimplySwordsAPI.registerWeaponType(ItemsRegistry.DARK_REPULSER.get(), Constants.location("simplyswords", "longsword"));
    }
}
