package net.ghostedhype.simplyanime.platform;

import net.ghostedhype.simplyanime.Constants;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.entity.PartEntity;

public class Platform {

    private static final ResourceLocation REACH_ID = Constants.id("rhitta_growth");

    public static Item.Properties weaponProperties(Tier tier, float damageModifier, float attackSpeed) {
        return new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(tier, (int) damageModifier, attackSpeed));
    }

    public static Entity partParent(Entity entity) {
        return entity instanceof PartEntity<?> part ? part.getParent() : entity;
    }

    public static boolean isPart(Entity entity) {
        return entity instanceof PartEntity<?>;
    }

    public static SoundEvent explosionSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    public static void ignite(Entity entity, int seconds) {
        entity.igniteForSeconds(seconds);
    }

    // reach as a multiple of the normal one, 1 puts it back
    public static void setReach(Player player, float scale) {
        setReach(player, Attributes.ENTITY_INTERACTION_RANGE, scale);
        setReach(player, Attributes.BLOCK_INTERACTION_RANGE, scale);
    }

    private static void setReach(Player player, Holder<Attribute> attribute, float scale) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(REACH_ID);
        if (scale > 1.0F) {
            instance.addTransientModifier(new AttributeModifier(REACH_ID, scale - 1.0F,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
