package net.ghostedhype.simplyanime.item;

import net.ghostedhype.simplyanime.ability.BladeSkill;
import net.ghostedhype.simplyanime.ability.BladeSkillManager;
import net.ghostedhype.simplyanime.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.sweenus.simplyswords.api.WeaponAbilityContext;

import java.util.List;

public class ElucidatorItem extends BladeItem {

    private static final int CRIMSON = 0xFF3B4E;
    private static final int EMBER = 0xD98A93;

    public ElucidatorItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public int getActivationCooldownTicks(ItemStack stack, WeaponAbilityContext context) {
        BladeSkill skill = context == null || context.actor() == null ? null
                : BladeSkillManager.skillFor(context.actor(), InteractionHand.MAIN_HAND);
        return BladeSkillManager.cooldown(skill == null ? BladeSkill.VORPAL_STRIKE : skill);
    }

    @Override
    protected void appendAbilityTooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        this.colored(tooltip, "tooltip1", CRIMSON, true);
        this.colored(tooltip, "tooltip2", EMBER, false);
        rightClick(tooltip);
        this.line(tooltip, "tooltip3");
        this.line(tooltip, "tooltip4");
        this.line(tooltip, "tooltip5");
        appendAbilityCooldownTooltip(tooltip, stack, Config.weapons.elucidator.cooldown);
        this.dualWield(tooltip, stack, "dual_main");
        appendAbilityManaCostTooltip(tooltip, stack);
    }
}
