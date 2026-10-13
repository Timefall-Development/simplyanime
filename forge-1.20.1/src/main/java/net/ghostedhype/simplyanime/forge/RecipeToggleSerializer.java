package net.ghostedhype.simplyanime.forge;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class RecipeToggleSerializer implements IConditionSerializer<RecipeToggleCondition> {

    @Override
    public void write(JsonObject json, RecipeToggleCondition value) {
        json.addProperty("weapon", value.weapon());
    }

    @Override
    public RecipeToggleCondition read(JsonObject json) {
        return new RecipeToggleCondition(GsonHelper.getAsString(json, "weapon"));
    }

    @Override
    public ResourceLocation getID() {
        return RecipeToggleCondition.ID;
    }
}
