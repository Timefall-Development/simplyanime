package net.hussain.simplyanime.autotest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.ability.EnumaElishAbilityManager;
import net.hussain.simplyanime.ability.InvertedSpearAbilityManager;
import net.hussain.simplyanime.entity.CruelSunEntity;
import net.hussain.simplyanime.registry.ItemsRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.sweenus.simplyswords.api.WeaponAbilityActivationSource;
import net.sweenus.simplyswords.api.WeaponAbilityContext;
import org.slf4j.Logger;

import java.util.List;

// /satest <sun|ea|field|strike>, casts at the nearest iron golem
public class CastCommand {

    private static final Logger LOG = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("satest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("what", StringArgumentType.word()).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    if (!(source.getEntity() instanceof LivingEntity caster)) {
                        source.sendFailure(Component.literal("Needs a living caster"));
                        return 0;
                    }
                    return cast(source.getLevel(), caster, StringArgumentType.getString(context, "what")) ? 1 : 0;
                })));
    }

    // registration and data that pictures don't show
    private static boolean check(ServerLevel world) {
        boolean tab = BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(Constants.SIMPLY_SWORDS_TAB);
        LOG.info("[autotest] tab {}: {}", Constants.SIMPLY_SWORDS_TAB, tab ? "found" : "MISSING");
        boolean recipe = world.getRecipeManager().getRecipeIds().anyMatch(id -> id.equals(Constants.id("enuma_elish")));
        LOG.info("[autotest] items recipe enuma_elish: {}", recipe ? "found" : "MISSING");
        for (String tag : new String[]{"uniques", "lootable_uniques", "swords", "conditional_uniques_type_1"}) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, Constants.location("simplyswords", tag));
            for (var item : ItemsRegistry.all()) {
                ItemStack stack = new ItemStack(item.get());
                LOG.info("[autotest] items {} in #{}: {}", stack.getItem(), key.location(), stack.is(key) ? "yes" : "NO");
            }
        }
        return tab && recipe;
    }

    private static boolean cast(ServerLevel world, LivingEntity caster, String what) {
        if (what.equals("check")) {
            return check(world);
        }
        List<IronGolem> golems = world.getEntitiesOfClass(IronGolem.class, caster.getBoundingBox().inflate(64), e -> true);
        LivingEntity target = golems.isEmpty() ? null : golems.get(0);
        for (IronGolem golem : golems) {
            if (golem.distanceToSqr(caster) < target.distanceToSqr(caster)) {
                target = golem;
            }
        }
        switch (what) {
            case "sun", "cast" -> {
                ItemStack stack = new ItemStack(ItemsRegistry.DIVINE_AXE_RHITTA.get());
                return CruelSunEntity.start(world, caster, target, stack, InteractionHand.MAIN_HAND);
            }
            case "ea" -> {
                return EnumaElishAbilityManager.start(context(world, caster, target, new ItemStack(ItemsRegistry.ENUMA_ELISH.get())));
            }
            case "field" -> {
                return InvertedSpearAbilityManager.startSlashField(context(world, caster, target,
                        new ItemStack(ItemsRegistry.INVERTED_SPEAR_OF_HEAVEN.get())));
            }
            case "strike" -> {
                return InvertedSpearAbilityManager.startTwinStrike(context(world, caster, target,
                        new ItemStack(ItemsRegistry.INVERTED_SPEAR_OF_HEAVEN.get())));
            }
            default -> {
                return false;
            }
        }
    }

    private static WeaponAbilityContext context(ServerLevel world, LivingEntity caster, LivingEntity target, ItemStack stack) {
        return WeaponAbilityContext.of(world, stack, caster, null, target, InteractionHand.MAIN_HAND, WeaponAbilityActivationSource.values()[0]);
    }
}
