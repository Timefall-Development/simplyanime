package net.hussain.simplyanime.config;

import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

public class DarkRepulserSettings extends ConfigSection {

    public float damageModifier = 3.0f;
    public float attackSpeed = -2.2f;
    public boolean lootable = true;

    // Horizontal Square
    @ValidatedInt.Restrict(min = 0)
    public int cooldown = 240;
    @ValidatedDouble.Restrict(min = 2.0, max = 48.0)
    public double waveRange = 13.0;
    @ValidatedFloat.Restrict(min = 0f)
    public float cutDamageScaling = 0.9f;
    @ValidatedFloat.Restrict(min = 0f)
    public float cutSpellScaling = 2.2f;
    @ValidatedDouble.Restrict(min = 3.0, max = 48.0)
    public double releaseRadius = 13.0;
    @ValidatedFloat.Restrict(min = 0f)
    public float releaseDamageScaling = 2.0f;
    @ValidatedFloat.Restrict(min = 0f)
    public float releaseSpellScaling = 5.0f;
    @ValidatedDouble.Restrict(min = 0.0, max = 5.0)
    public double knockback = 1.3;
    public boolean nameText = true;
    @ValidatedFloat.Restrict(min = 0f, max = 16f)
    public float volume = 2.0f;
}
