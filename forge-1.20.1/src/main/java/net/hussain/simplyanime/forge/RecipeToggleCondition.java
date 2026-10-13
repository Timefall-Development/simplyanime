package net.hussain.simplyanime.forge;

import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

// lets a recipe be switched off from the config: "type": "simplyanime:craftable", "weapon": "enuma_elish"
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
