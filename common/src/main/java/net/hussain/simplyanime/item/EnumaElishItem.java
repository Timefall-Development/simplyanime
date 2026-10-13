package net.hussain.simplyanime.item;

import net.hussain.simplyanime.ability.EnumaElishAbilityManager;
import net.hussain.simplyanime.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.sweenus.simplyswords.api.WeaponAbilityContext;

import java.util.List;

public class EnumaElishItem extends AnimeWeaponItem {

    public EnumaElishItem(Tier tier, Properties properties) {
        super(tier, properties, "claymore");
    }

    @Override
    public boolean canActivate(WeaponAbilityContext context) {
        if (!ready(context) || EnumaElishAbilityManager.isBusy(context.world(), context.actor())) {
            return false;
        }
        // mobs only let it go when they have something to point it at
        return context.actor() instanceof Player || context.target() != null;
    }

    @Override
    public boolean activate(WeaponAbilityContext context) {
        return EnumaElishAbilityManager.start(context);
    }

    @Override
    public int getActivationCooldownTicks(ItemStack stack, WeaponAbilityContext context) {
        return Config.weapons.enuma_elish.cooldown;
    }

    @Override
    protected void appendAbilityTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        this.title(tooltip, "tooltip1");
        this.line(tooltip, "tooltip2");
        rightClick(tooltip);
        this.line(tooltip, "tooltip3");
        this.line(tooltip, "tooltip4");
        this.line(tooltip, "tooltip5");
        appendAbilityCooldownTooltip(tooltip, stack, Config.weapons.enuma_elish.cooldown);
        appendAbilityManaCostTooltip(tooltip, stack);
    }
}
