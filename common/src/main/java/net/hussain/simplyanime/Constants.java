package net.hussain.simplyanime;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

    public static final String MOD_ID = "simplyanime";
    public static final String MOD_NAME = "Simply Anime";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static ResourceLocation tryId(String path) {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }
}