package net.ghostedhype.simplyanime.item;

import net.ghostedhype.simplyanime.ability.InvertedSpearAbilityManager;
import net.ghostedhype.simplyanime.config.Config;
import net.ghostedhype.simplyanime.config.InvertedSpearSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.sweenus.simplyswords.api.WeaponAbilityContext;
import net.sweenus.simplyswords.util.Styles;

import java.util.List;

public class InvertedSpearItem extends AnimeWeaponItem {

    public InvertedSpearItem(Tier tier, Properties properties) {
        super(tier, properties, "sai");
    }

    @Override
    public boolean canActivate(WeaponAbilityContext context) {
        return ready(context) && !InvertedSpearAbilityManager.isBusy(context.world(), context.actor());
    }

    @Override
    public boolean activate(WeaponAbilityContext context) {
        if (InvertedSpearAbilityManager.wantsTwinStrike(context)) {
            return InvertedSpearAbilityManager.startTwinStrike(context);
        }
        return InvertedSpearAbilityManager.startSlashField(context);
    }

    @Override
    public int getActivationCooldownTicks(ItemStack stack, WeaponAbilityContext context) {
        InvertedSpearSettings settings = Config.weapons.inverted_spear_of_heaven;
        return InvertedSpearAbilityManager.wantsTwinStrike(context) ? settings.strikeCooldown : settings.fieldCooldown;
    }

    @Override
    protected void appendAbilityTooltip(ItemStack stack, List<Component> tooltip) {
        InvertedSpearSettings settings = Config.weapons.inverted_spear_of_heaven;
        tooltip.add(Component.literal(""));
        this.title(tooltip, "tooltip1");
        this.line(tooltip, "tooltip2");
        rightClick(tooltip);
        this.line(tooltip, "tooltip5");
        this.line(tooltip, "tooltip6");
        appendAbilityCooldownTooltip(tooltip, stack, settings.strikeCooldown);
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable(this.getDescriptionId() + ".sneak_right_click").setStyle(Styles.RIGHT_CLICK));
        this.line(tooltip, "tooltip3");
        this.line(tooltip, "tooltip4");
        appendAbilityCooldownTooltip(tooltip, stack, settings.fieldCooldown);
        appendAbilityManaCostTooltip(tooltip, stack);
    }
}
