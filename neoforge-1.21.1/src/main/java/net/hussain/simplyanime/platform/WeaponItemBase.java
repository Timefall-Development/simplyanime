package net.hussain.simplyanime.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.client.api.SimplySwordsClientAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.interfaces.UniqueWeaponActiveAbility;

import java.util.ArrayList;
import java.util.List;

// the Simply Swords hooks whose signatures change between versions
public abstract class WeaponItemBase extends UniqueSwordItem implements UniqueWeaponActiveAbility {

    protected WeaponItemBase(Tier tier, Properties properties) {
        super(tier, properties);
    }

    protected abstract void appendAbilityTooltip(ItemStack stack, List<Component> tooltip);

    @Override
    protected InteractionResultHolder<ItemStack> useUniqueWeapon(Level level, Player user, InteractionHand hand) {
        return useFromDefaultInput(level, user, hand);
    }

    @Override
    protected void appendUniqueWeaponTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        this.appendAbilityTooltip(stack, tooltip);
        appendSharedUniqueWeaponTooltip(stack, context, tooltip, flag);
        SimplySwordsClientAPI.appendSpellScaleTooltip(tooltip, "arcane");
    }

    // no oracle index pages for the addon yet, so skip the links but keep the gem sockets
    @Override
    protected void generateDynamicTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        List<Component> sockets = new ArrayList<>();
        SimplySwordsAPI.appendTooltipGemSocketLogic(stack, context, sockets, flag);
        while (!sockets.isEmpty() && sockets.get(0).getString().isEmpty()) {
            sockets.remove(0);
        }
        if (!sockets.isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.addAll(sockets);
        }
    }
}
