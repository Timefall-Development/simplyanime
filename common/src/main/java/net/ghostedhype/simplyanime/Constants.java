package net.ghostedhype.simplyanime;

import net.minecraft.resources.ResourceLocation;

public class Constants {

    public static final String MOD_ID = "simplyanime";
    public static final ResourceLocation SIMPLY_SWORDS_TAB = location("simplyswords", "simplyswords");

    public static ResourceLocation id(String path) {
        return location(MOD_ID, path);
    }

    public static ResourceLocation location(String namespace, String path) {
        return ResourceLocation.tryBuild(namespace, path);
    }
}
