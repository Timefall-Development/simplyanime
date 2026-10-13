package net.ghostedhype.simplyanime.registry;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.util.platform.Registrar;
import me.fzzyhmstrs.fzzy_config.util.platform.RegistrySupplier;
import net.ghostedhype.simplyanime.Constants;
import net.ghostedhype.simplyanime.config.Config;
import net.ghostedhype.simplyanime.config.RhittaConfig;
import net.ghostedhype.simplyanime.item.DarkRepulserItem;
import net.ghostedhype.simplyanime.item.DivineAxeRhittaItem;
import net.ghostedhype.simplyanime.item.ElucidatorItem;
import net.ghostedhype.simplyanime.item.EnumaElishItem;
import net.ghostedhype.simplyanime.item.InvertedSpearItem;
import net.ghostedhype.simplyanime.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.sweenus.simplyswords.item.ModToolMaterial;

import java.util.List;

public class ItemsRegistry {

    private static final Registrar<Item> ITEMS = ConfigApiJava.platform().createRegistrar(Constants.MOD_ID, BuiltInRegistries.ITEM);

    public static final RegistrySupplier<Item> INVERTED_SPEAR_OF_HEAVEN = ITEMS.register("inverted_spear_of_heaven", () ->
            new InvertedSpearItem(ModToolMaterial.UNIQUE, Platform.weaponProperties(ModToolMaterial.UNIQUE,
                    Config.weapons.inverted_spear_of_heaven.damageModifier, Config.weapons.inverted_spear_of_heaven.attackSpeed)));

    public static final RegistrySupplier<Item> ENUMA_ELISH = ITEMS.register("enuma_elish", () ->
            new EnumaElishItem(ModToolMaterial.UNIQUE, Platform.weaponProperties(ModToolMaterial.UNIQUE,
                    Config.weapons.enuma_elish.damageModifier, Config.weapons.enuma_elish.attackSpeed)));

    public static final RegistrySupplier<Item> DIVINE_AXE_RHITTA = ITEMS.register("divine_axe_rhitta", () ->
            new DivineAxeRhittaItem(ModToolMaterial.UNIQUE, Platform.weaponProperties(ModToolMaterial.UNIQUE,
                    RhittaConfig.get().axe.damageModifier, RhittaConfig.get().axe.attackSpeed)));

    public static final RegistrySupplier<Item> ELUCIDATOR = ITEMS.register("elucidator", () ->
            new ElucidatorItem(ModToolMaterial.UNIQUE, Platform.weaponProperties(ModToolMaterial.UNIQUE,
                    Config.weapons.elucidator.damageModifier, Config.weapons.elucidator.attackSpeed)));

    public static final RegistrySupplier<Item> DARK_REPULSER = ITEMS.register("dark_repulser", () ->
            new DarkRepulserItem(ModToolMaterial.UNIQUE, Platform.weaponProperties(ModToolMaterial.UNIQUE,
                    Config.weapons.dark_repulser.damageModifier, Config.weapons.dark_repulser.attackSpeed)));

    public static void init() {
        ITEMS.init();
    }

    // everything that goes in the Simply Swords creative tab
    public static List<RegistrySupplier<Item>> all() {
        return List.of(INVERTED_SPEAR_OF_HEAVEN, ENUMA_ELISH, DIVINE_AXE_RHITTA, ELUCIDATOR, DARK_REPULSER);
    }
}
