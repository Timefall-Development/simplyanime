package net.hussain.simplyanime.platform;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;
import net.sweenus.simplyswords.item.LegacyWeaponAttributes;

import java.util.UUID;

// the few things the shared code needs that Forge 1.20.1 does its own way
public class Platform {

    private static final UUID REACH_ID = UUID.fromString("5f2a7d1e-3c4b-4e8a-9b6d-2e1f0a9c8b71");

    public static Item.Properties weaponProperties(Tier tier, float damageModifier, float attackSpeed) {
        return LegacyWeaponAttributes.configure(new Item.Properties().fireResistant(), (int) damageModifier, attackSpeed);
    }

    // the whole mob for a piece of a multipart one, the entity itself otherwise
    public static Entity partParent(Entity entity) {
        return entity instanceof PartEntity<?> part ? part.getParent() : entity;
    }

    public static boolean isPart(Entity entity) {
        return entity instanceof PartEntity<?>;
    }

    public static SoundEvent explosionSound() {
        return SoundEvents.GENERIC_EXPLODE;
    }

    public static void ignite(Entity entity, int seconds) {
        entity.setSecondsOnFire(seconds);
    }

    // reach as a multiple of the normal one, 1 puts it back
    public static void setReach(Player player, float scale) {
        setReach(player, ForgeMod.ENTITY_REACH.get(), scale);
        setReach(player, ForgeMod.BLOCK_REACH.get(), scale);
    }

    private static void setReach(Player player, Attribute attribute, float scale) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(REACH_ID);
        if (scale > 1.0F) {
            instance.addTransientModifier(new AttributeModifier(REACH_ID, "Rhitta growth", scale - 1.0F,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
