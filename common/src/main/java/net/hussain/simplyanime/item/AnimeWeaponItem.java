package net.hussain.simplyanime.item;

import net.hussain.simplyanime.platform.WeaponItemBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.sweenus.simplyswords.api.WeaponAbilityContext;
import net.sweenus.simplyswords.util.Styles;

import java.util.List;

public abstract class AnimeWeaponItem extends WeaponItemBase {

    private final String weaponType;

    protected AnimeWeaponItem(Tier tier, Properties properties, String weaponType) {
        super(tier, properties);
        this.weaponType = weaponType;
    }

    @Override
    public String getTooltipWeaponType(ItemStack stack) {
        return this.weaponType;
    }

    // someone alive holding a weapon that isn't about to break
    protected static boolean ready(WeaponAbilityContext context) {
        return context != null
                && context.world() != null
                && context.actor() != null
                && context.actor().isAlive()
                && context.stack() != null
                && !context.stack().isEmpty()
                && context.stack().getDamageValue() < context.stack().getMaxDamage() - 1;
    }

    protected void line(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable(this.getDescriptionId() + "." + key).setStyle(Styles.TEXT));
    }

    protected void title(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable(this.getDescriptionId() + "." + key).setStyle(Styles.ABILITY));
    }

    protected static void rightClick(List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.simplyswords.onrightclick").setStyle(Styles.RIGHT_CLICK));
    }
}
