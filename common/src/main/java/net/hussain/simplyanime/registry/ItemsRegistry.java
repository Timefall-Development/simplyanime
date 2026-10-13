package net.hussain.simplyanime.registry;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.util.platform.Registrar;
import me.fzzyhmstrs.fzzy_config.util.platform.RegistrySupplier;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.config.Config;
import net.hussain.simplyanime.config.RhittaConfig;
import net.hussain.simplyanime.item.DivineAxeRhittaItem;
import net.hussain.simplyanime.item.EnumaElishItem;
import net.hussain.simplyanime.item.InvertedSpearItem;
import net.hussain.simplyanime.platform.Platform;
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

    public static void init() {
        ITEMS.init();
    }

    // everything that goes in the Simply Swords creative tab
    public static List<RegistrySupplier<Item>> all() {
        return List.of(INVERTED_SPEAR_OF_HEAVEN, ENUMA_ELISH, DIVINE_AXE_RHITTA);
    }
}
