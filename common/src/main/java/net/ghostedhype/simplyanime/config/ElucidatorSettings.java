package net.ghostedhype.simplyanime.config;

import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

public class ElucidatorSettings extends ConfigSection {

    public float damageModifier = 4.0f;
    public float attackSpeed = -2.4f;
    public boolean lootable = true;

    // Vorpal Strike
    @ValidatedInt.Restrict(min = 0)
    public int cooldown = 280;
    @ValidatedDouble.Restrict(min = 0.0, max = 24.0)
    public double dashDistance = 6.0;
    @ValidatedDouble.Restrict(min = 4.0, max = 96.0)
    public double lanceLength = 34.0;
    @ValidatedDouble.Restrict(min = 0.5, max = 8.0)
    public double lanceRadius = 2.2;
    @ValidatedFloat.Restrict(min = 0f)
    public float damageScaling = 4.0f;
    @ValidatedFloat.Restrict(min = 0f)
    public float spellScaling = 10.0f;
    @ValidatedDouble.Restrict(min = 0.0, max = 5.0)
    public double knockback = 1.5;
    public boolean nameText = true;
    // volume above 1 only makes it carry further
    @ValidatedFloat.Restrict(min = 0f, max = 16f)
    public float volume = 2.0f;
}
