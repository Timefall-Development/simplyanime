package net.hussain.simplyanime.item;

import net.hussain.simplyanime.ability.EnumaElishAbilityManager;
import net.hussain.simplyanime.animation.AttackStyle;
import net.hussain.simplyanime.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.sweenus.simplyswords.api.WeaponAbilityContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnumaElishItem extends AnimeWeaponItem {

    public EnumaElishItem(Tier tier, Item.Properties properties) {
        super(tier, properties, "claymore");
    }

    // its left click is the beam, it never swings
    @Override
    @Nullable
    public AttackStyle attackStyle() {
        return null;
    }

    @Override
    public boolean canActivate(WeaponAbilityContext context) {
        if (!ready(context) || EnumaElishAbilityManager.isBusy(context.world(), context.actor())) {
            return false;
        }
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
