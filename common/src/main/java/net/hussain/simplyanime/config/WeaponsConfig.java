package net.hussain.simplyanime.config;

import net.hussain.simplyanime.Constants;

public class WeaponsConfig extends me.fzzyhmstrs.fzzy_config.config.Config {

    public WeaponsConfig() {
        super(Constants.tryId("weapons"));
    }

    public InvertedSpearSettings inverted_spear_of_heaven = new InvertedSpearSettings();
    public EnumaElishSettings enuma_elish = new EnumaElishSettings();
    public ElucidatorSettings elucidator = new ElucidatorSettings();
    public DarkRepulserSettings dark_repulser = new DarkRepulserSettings();
    public StarburstSettings starburst_stream = new StarburstSettings();
}
