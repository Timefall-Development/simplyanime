package net.ghostedhype.simplyanime.forge;

import net.ghostedhype.simplyanime.Constants;
import net.ghostedhype.simplyanime.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

public record RecipeToggleCondition(String weapon) implements ICondition {

    static final ResourceLocation ID = Constants.id("craftable");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return switch (this.weapon) {
            case "enuma_elish" -> Config.weapons.enuma_elish.craftable;
            default -> true;
        };
    }
}
